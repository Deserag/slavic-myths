#!/usr/bin/env python3
"""Convert only the 15 approved Project #9 Drafts into repository-backed Issues.

This is an *in-place* ProjectV2 mutation. It preserves the Project item rather
than creating an orphan repo Issue and subsequently adding it to the board.
Safe to retry: already converted items are skipped, not duplicated.
"""
import json
import os
import re
import urllib.error
import urllib.request
from pathlib import Path

cfg = json.loads(Path("tools/project-migration/project-draft-tasks.json").read_text("utf-8"))
token = os.environ.get("GH_PROJECTS_TOKEN")
if not token:
    raise RuntimeError("GitHub Project token not configured")

def gql(query, **variables):
    request = urllib.request.Request(
        "https://api.github.com/graphql",
        data=json.dumps({"query": query, "variables": variables}).encode("utf-8"),
        headers={
          "Authorization": "Bearer " + token,
          "Accept": "application/vnd.github+json",
          "Content-Type": "application/json",
          "X-GitHub-Api-Version": "2022-11-28",
          "User-Agent": "SlavicMyths-Project9-Convert",
        })
    try:
        with urllib.request.urlopen(request, timeout=60) as resp:
            data = json.load(resp)
    except urllib.error.HTTPError as exc:
        raise RuntimeError("GitHub HTTP " + str(exc.code) + ": " + exc.read().decode("utf-8")[:450]) from exc
    if data.get("errors"):
        raise RuntimeError("GitHub GraphQL: " + json.dumps(data["errors"], ensure_ascii=False)[:1000])
    return data["data"]

PROJECT_QUERY = """
query($owner:String!,$number:Int!,$cursor:String,$repo:String!) {
  repository(owner:$owner,name:$repo) { id }
  user(login:$owner) {
    projectV2(number:$number) {
      id title
      items(first:100,after:$cursor) {
        pageInfo {hasNextPage endCursor}
        nodes {
          id
          fieldValues(first:20) { nodes {
            ... on ProjectV2ItemFieldSingleSelectValue {
              name field { ... on ProjectV2SingleSelectField { name } }
            }
          } }
          content {
            __typename
            ... on DraftIssue { id title body }
            ... on Issue { id number title body url repository {nameWithOwner} }
          }
        }
      }
    }
  }
}
"""
CONVERT = """
mutation($item:ID!,$repository:ID!) {
 convertProjectV2DraftIssueItemToIssue(input:{itemId:$item,repositoryId:$repository}) {
   item {
     id content { __typename ... on Issue { number url title repository {nameWithOwner} } }
   }
 }
}
"""

def read():
    cursor = None
    all_items = []
    project_id = None
    repository_id = None
    while True:
        result = gql(PROJECT_QUERY, owner=cfg["owner"], number=cfg["projectNumber"],
                     repo=cfg["repo"], cursor=cursor)
        project = (result.get("user") or {}).get("projectV2")
        repository = result.get("repository")
        if not project or not repository:
            raise RuntimeError("Project #9 or target repository inaccessible")
        project_id = project["id"]
        repository_id = repository["id"]
        page = project["items"]
        all_items.extend(page["nodes"])
        if not page["pageInfo"]["hasNextPage"]:
            break
        cursor = page["pageInfo"]["endCursor"]
    return project_id, repository_id, all_items

def get_status(item):
    for value in item.get("fieldValues", {}).get("nodes", []):
        if value and (value.get("field") or {}).get("name") == "Status":
            return value.get("name")
    return None

pid, rid, items = read()
wanted = {t["key"]: t for t in cfg["tasks"]}
selected = {}
status_snapshot = {}
for item in items:
    content = item.get("content") or {}
    body = content.get("body") or ""
    m = re.search(r"PROJECT9_CONCEPT_KEY:([a-z0-9-]+)", body)
    if not m or m.group(1) not in wanted:
        continue
    key = m.group(1)
    if key in selected:
        raise RuntimeError("Duplicate Project #9 cards for " + key + "; conversion stopped")
    selected[key] = item

missing = set(wanted) - set(selected)
if missing:
    raise RuntimeError("Project cards missing before conversion: " + ", ".join(sorted(missing)))
status_snapshot = {key: get_status(item) for key, item in selected.items()}
if any(v is None for v in status_snapshot.values()):
    raise RuntimeError("Some Project items have no Status: " + str(status_snapshot))
print("PROJECT", pid, "CARDS", len(selected), "TARGET REPOSITORY", cfg["repo"], flush=True)
converted = []
for task in cfg["tasks"]:
    key = task["key"]
    item = selected[key]
    before = status_snapshot[key]
    content = item["content"]
    if content["__typename"] == "Issue":
        if content.get("repository", {}).get("nameWithOwner") != cfg["owner"] + "/" + cfg["repo"]:
            raise RuntimeError("Converted issue points to unexpected repository for " + key)
        print("ALREADY CONVERTED", key, content["url"], "status", before, flush=True)
        continue
    if content["__typename"] != "DraftIssue":
        raise RuntimeError("Unexpected content type for " + key)
    result = gql(CONVERT, item=item["id"], repository=rid)
    converted_item = result["convertProjectV2DraftIssueItemToIssue"]["item"]
    if not converted_item or converted_item["content"]["__typename"] != "Issue":
        raise RuntimeError("Conversion did not return an Issue for " + key)
    issue = converted_item["content"]
    if issue.get("repository", {}).get("nameWithOwner") != cfg["owner"] + "/" + cfg["repo"]:
        raise RuntimeError("Converted into wrong repository for " + key)
    converted.append(key)
    print("CONVERTED", key, "Issue #" + str(issue["number"]), issue["url"], flush=True)

_, _, check = read()
state = {}
for item in check:
    content = item.get("content") or {}
    m = re.search(r"PROJECT9_CONCEPT_KEY:([a-z0-9-]+)", content.get("body") or "")
    if m and m.group(1) in wanted:
        state[m.group(1)] = item
errors = []
for task in cfg["tasks"]:
    key = task["key"]
    item = state.get(key)
    if not item:
        errors.append("missing " + key)
        continue
    content = item.get("content") or {}
    if content.get("__typename") != "Issue":
        errors.append("not converted " + key)
    if get_status(item) != status_snapshot[key]:
        errors.append("Status changed during conversion for " + key + ": " + str(status_snapshot[key]) + " → " + str(get_status(item)))
    if content.get("repository", {}).get("nameWithOwner") != cfg["owner"] + "/" + cfg["repo"]:
        errors.append("wrong repo " + key)
    print("CHECK", key, content.get("url"), "Status", get_status(item), flush=True)
if errors:
    raise RuntimeError("Project conversion verification: " + "; ".join(errors))
print("SUCCESS", len(wanted), "Project-linked GitHub Issues; converted now", len(converted), flush=True)
