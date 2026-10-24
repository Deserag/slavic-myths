#!/usr/bin/env python3
"""Move only owner-approved Project #9 draft cards from Концепция to Ожидание.

The desired state is recorded in project-draft-tasks.json. Safe for re-runs:
already-ready and subsequently advanced items are never reset. No repo Issues
are created, reopened, or changed by this script.
"""
import json
import os
import urllib.request
import urllib.error
from pathlib import Path

cfg = json.loads(Path("tools/project-migration/project-draft-tasks.json").read_text("utf-8"))
approved = {t["key"]: t for t in cfg["tasks"] if t["status"] == "Ожидание"}
assert approved, "No approved status updates requested."
token = os.environ.get("GH_PROJECTS_TOKEN")
if not token:
    raise RuntimeError("Missing GH_PROJECTS_TOKEN")
endpoint = "https://api.github.com/graphql"

def gql(query, **variables):
    payload = json.dumps({"query": query, "variables": variables}).encode()
    req = urllib.request.Request(endpoint, data=payload, headers={
        "Authorization": "Bearer " + token,
        "Accept": "application/vnd.github+json",
        "Content-Type": "application/json",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "SlavicMyths-Project9-ReadyGate",
    })
    try:
        with urllib.request.urlopen(req, timeout=50) as resp:
            out = json.load(resp)
    except urllib.error.HTTPError as err:
        raise RuntimeError("GitHub HTTP " + str(err.code)) from err
    if out.get("errors"):
        raise RuntimeError("GitHub GraphQL failed: " + json.dumps(out["errors"], ensure_ascii=False)[:600])
    return out["data"]

PROJECT_QUERY = """
query($login:String!,$number:Int!,$after:String) {
  user(login:$login) {
    projectV2(number:$number) {
      id title
      fields(first:100) { nodes {
        ... on ProjectV2SingleSelectField { id name options { id name } }
      } }
      items(first:100,after:$after) {
        pageInfo { hasNextPage endCursor }
        nodes {
          id
          content { ... on DraftIssue { id title body } ... on Issue { id title body } }
        }
      }
    }
  }
}
"""
CURRENT_STATUS_QUERY = """
query($id:ID!) {
  node(id:$id) {
    ... on ProjectV2Item {
      id
      fieldValues(first:30) { nodes {
        ... on ProjectV2ItemFieldSingleSelectValue {
          name
          field { ... on ProjectV2SingleSelectField { name } }
        }
      } }
    }
  }
}
"""
SET_STATUS = """
mutation($project:ID!,$item:ID!,$field:ID!,$option:String!) {
  updateProjectV2ItemFieldValue(input:{
    projectId:$project, itemId:$item, fieldId:$field,
    value:{singleSelectOptionId:$option}
  }) { projectV2Item { id } }
}
"""

def status(item_id):
    result = gql(CURRENT_STATUS_QUERY, id=item_id)
    item = result.get("node")
    if item is None:
        raise RuntimeError("Missing Project item: " + item_id)
    values = item.get("fieldValues", {}).get("nodes", [])
    return next((v["name"] for v in values
                 if v and (v.get("field") or {}).get("name") == "Status"), None)

cursor = None
card_by_key = {}
project = None
while True:
    result = gql(PROJECT_QUERY, login=cfg["owner"], number=cfg["projectNumber"], after=cursor)
    project = (result.get("user") or {}).get("projectV2")
    if project is None:
        raise RuntimeError("Project #9 is not accessible")
    for item in project["items"]["nodes"]:
        content = item.get("content") or {}
        body = content.get("body") or ""
        for key in approved:
            if "<!-- PROJECT9_CONCEPT_KEY:" + key + " -->" in body:
                if key in card_by_key:
                    raise RuntimeError("Duplicate Project cards for " + key)
                card_by_key[key] = item["id"]
    page = project["items"]["pageInfo"]
    if not page["hasNextPage"]:
        break
    cursor = page["endCursor"]

missing = set(approved) - set(card_by_key)
if missing:
    raise RuntimeError("Missing Project Issue cards: " + ", ".join(sorted(missing)))
field = next((f for f in project["fields"]["nodes"] if f and f.get("name") == "Status"), None)
if not field:
    raise RuntimeError("Project Status field missing")
ready = next((opt for opt in field["options"] if opt["name"] == "Ожидание"), None)
if not ready:
    raise RuntimeError("Project Ожидание status option missing")

print("PROJECT", project["title"], "approved", len(approved), flush=True)
changed = []
preserved_manual = {}
for key, task in approved.items():
    item = card_by_key[key]
    before = status(item)
    if before == "Концепция":
        gql(SET_STATUS, project=project["id"], item=item, field=field["id"], option=ready["id"])
        changed.append(key)
        print("UPDATED", key, "Концепция → Ожидание", flush=True)
    elif before == "Ожидание":
        print("ALREADY READY", key, flush=True)
    else:
        preserved_manual[key] = before
        print("PRESERVE OWNER STATUS", key, before, flush=True)

not_verified = []
for key in approved:
    after = status(card_by_key[key])
    print("VERIFIED", key, after, flush=True)
    expected = preserved_manual.get(key, "Ожидание")
    if after != expected:
        not_verified.append(key)
if not_verified:
    raise RuntimeError("Not ready on board: " + ", ".join(not_verified))
print("SUCCESS: examined", len(approved), "Project #9 cards;", len(changed), "promoted;", len(preserved_manual), "owner changes preserved", flush=True)
