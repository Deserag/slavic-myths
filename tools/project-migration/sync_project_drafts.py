#!/usr/bin/env python3
"""Synchronize approved concepts as native GitHub Projects v2 DRAFT items.

Never opens or reopens repository Issues. Legacy closed Issues are read-only
sources for detailed specs. Uses the Actions secret SLAVIC_PROJECTS_TOKEN.
Safe to re-run: marker-based idempotency and no changes to unrelated cards.
"""
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

CFG = json.loads(Path("tools/project-migration/project-draft-tasks.json").read_text(encoding="utf-8"))
TOKEN = os.environ.get("GH_PROJECTS_TOKEN", "")
if not TOKEN:
    sys.exit("SLAVIC_PROJECTS_TOKEN missing: do not put tokens in source, chat or logs.")
API = "https://api.github.com"
GRAPH = API + "/graphql"

def request(url, payload=None):
    headers = {
        "Authorization": "Bearer " + TOKEN,
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "SlavicMyths-Project9-DraftSync",
    }
    if payload is not None:
        headers["Content-Type"] = "application/json"
    data = json.dumps(payload, ensure_ascii=False).encode("utf-8") if payload is not None else None
    req = urllib.request.Request(url, data=data, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=60) as response:
            return json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        raise RuntimeError("GitHub HTTP %s %s" % (exc.code, exc.read().decode("utf-8")[:600])) from exc

def gql(query, **variables):
    response = request(GRAPH, {"query": query, "variables": variables})
    if response.get("errors"):
        raise RuntimeError("GitHub GraphQL: " + json.dumps(response["errors"], ensure_ascii=False)[:1000])
    return response["data"]

PROJECT_QUERY = """
query($login:String!,$number:Int!,$after:String) {
  user(login:$login) {
    projectV2(number:$number) {
      id title
      fields(first:100) { nodes {
        ... on ProjectV2SingleSelectField { id name options { id name } }
        ... on ProjectV2Field { id name dataType }
      } }
      items(first:100,after:$after) {
        pageInfo { hasNextPage endCursor }
        nodes {
          id
          content {
            __typename
            ... on DraftIssue { id title body }
            ... on Issue { number title state repository { nameWithOwner } }
          }
        }
      }
    }
  }
}
"""

def read_project():
    cursor = None
    project = None
    items = []
    while True:
        data = gql(PROJECT_QUERY, login=CFG["owner"], number=CFG["projectNumber"], after=cursor)
        current = (data.get("user") or {}).get("projectV2")
        if not current:
            raise RuntimeError("Project #9 not found or token missing project permission")
        project = current
        page = current["items"]
        items.extend(page["nodes"])
        if not page["pageInfo"]["hasNextPage"]:
            break
        cursor = page["pageInfo"]["endCursor"]
    return project, items

project, items = read_project()
print("PROJECT", project["title"], "items-before", len(items))
pid = project["id"]
fields = {x["name"]: x for x in project["fields"]["nodes"] if x and x.get("name")}
if "Status" not in fields:
    raise RuntimeError("Project lacks Status field")
if not any(x["name"] == "Концепция" for x in fields["Status"].get("options", [])):
    raise RuntimeError("Project Status lacks Концепция option")

def update_field(item_id, name, value):
    field = fields.get(name)
    if not field or value is None:
        return
    if name == "Порядок":
        value_entry = {"number": float(value)}
    else:
        option = next((x for x in field.get("options", []) if x["name"] == value), None)
        if not option:
            if name == "Status":
                raise RuntimeError("Missing required Status option " + str(value))
            print("WARNING skip unconfigured field:", name, value)
            return
        value_entry = {"singleSelectOptionId": option["id"]}
    gql("""
    mutation($input:UpdateProjectV2ItemFieldValueInput!) {
      updateProjectV2ItemFieldValue(input:$input) { projectV2Item { id } }
    }
    """, input={
        "projectId": pid, "itemId": item_id, "fieldId": field["id"],
        "value": value_entry
    })

def build_body(task):
    legacy = task.get("legacy")
    source = ""
    if legacy:
        issue = request(API + "/repos/%s/%s/issues/%d" %
                        (CFG["owner"], CFG["repo"], legacy))
        source = issue.get("body") or ""
    marker = "<!-- PROJECT9_CONCEPT_KEY:" + task["key"] + " -->"
    return "\n\n".join([
        marker,
        "> **РАБОЧАЯ КАРТОЧКА ТОЛЬКО В PROJECT №9 · Статус КОНЦЕПЦИЯ.** "
        "Предыдущие Issue закрыты и служат только архивом. "
        "Ссылки на старые #issues внутри архива — НЕ текущие задачи. "
        "Реализация не разрешена до отдельного решения владельца; "
        "не путать готовую концепцию с выполненным кодом.",
        "# Актуальные решения и задание",
        task["intro"],
        "**Головной проект:** https://github.com/users/Deserag/projects/9/views/1 . "
        "**Релиз:** " + task["release"] + ". "
        "**Этап:** Концепция. "
        "**Направление:** " + task["direction"] + ".",
        "**Зависимости:** задачи этой же серии по их заголовкам в Project №9; "
        "источники предыдущих ТЗ сохранены ниже для подробных требований.",
        "## Архив подробной постановки (только справочный материал)",
        ("Исторический источник: https://github.com/%s/%s/issues/%d" %
         (CFG["owner"], CFG["repo"], legacy)) if legacy else "Нет архивной Issue.",
        source,
    ])

# Non-secret stable item IDs recorded from earlier GitHub Actions run logs.
# Some recently created cards were absent from paginated project listing.
KNOWN_DRAFT_IDS = {
"nether-epic":["PVTI_lAHOBeEK2c4Bn9vzzhDGrss"],
"nether-201-worldgen":["PVTI_lAHOBeEK2c4Bn9vzzhDGrvg"],
"nether-201-height":["PVTI_lAHOBeEK2c4Bn9vzzhDGrx4","PVTI_lAHOBeEK2c4Bn9vzzhDGtKc"],
"nether-201-terrain":["PVTI_lAHOBeEK2c4Bn9vzzhDGr00","PVTI_lAHOBeEK2c4Bn9vzzhDGtNc"],
"nether-201-lava":["PVTI_lAHOBeEK2c4Bn9vzzhDGr3k","PVTI_lAHOBeEK2c4Bn9vzzhDGtR4"],
"nether-201-vanilla":["PVTI_lAHOBeEK2c4Bn9vzzhDGr6k"],
"nether-201-soul":["PVTI_lAHOBeEK2c4Bn9vzzhDGr-E"],
"nether-biomes":["PVTI_lAHOBeEK2c4Bn9vzzhDGsBw"],
"navya-main":["PVTI_lAHOBeEK2c4Bn9vzzhDGsEg"],
"navya-2021":["PVTI_lAHOBeEK2c4Bn9vzzhDGsHk","PVTI_lAHOBeEK2c4Bn9vzzhDGtUc"],
"navya-2022":["PVTI_lAHOBeEK2c4Bn9vzzhDGsKk"],
"navya-2023":["PVTI_lAHOBeEK2c4Bn9vzzhDGsMo"],
"navya-2024":["PVTI_lAHOBeEK2c4Bn9vzzhDGsQA"],
"navya-2025":["PVTI_lAHOBeEK2c4Bn9vzzhDGsSk"],
"navya-2026":["PVTI_lAHOBeEK2c4Bn9vzzhDGsVA"]
}
NODE_QUERY = """
query($id:ID!) {
  node(id:$id) {
    ... on ProjectV2Item {
      id
      content { __typename ... on DraftIssue { id title body } }
    }
  }
}
"""
def known_draft(key):
    valid = []
    for node_id in KNOWN_DRAFT_IDS.get(key, []):
        item = gql(NODE_QUERY, id=node_id).get("node")
        c = (item or {}).get("content") or {}
        if c.get("__typename") == "DraftIssue" and "PROJECT9_CONCEPT_KEY:" + key in (c.get("body") or ""):
            valid.append(item)
    return valid

by_key = {}
old_issues = {}
for item in items:
    c = item.get("content") or {}
    if c.get("__typename") == "DraftIssue":
        k = re.search(r"PROJECT9_CONCEPT_KEY:([a-z0-9-]+)", c.get("body") or "")
        if k:
            by_key[k.group(1)] = item
        by_key.setdefault("title:" + c.get("title", ""), item)
    elif c.get("__typename") == "Issue" and c.get("repository", {}).get("nameWithOwner") == CFG["owner"] + "/" + CFG["repo"]:
        old_issues[c["number"]] = item

counts = {"created": 0, "existing": 0, "field_updates": 0}
for task in CFG["tasks"]:
    key = task["key"]
    existing = by_key.get(key) or by_key.get("title:" + task["title"])
    if not existing and key in KNOWN_DRAFT_IDS:
        matches = known_draft(key)
        if matches:
            existing = matches[0]
            print("FOUND DIRECTLY by node id, not list:", key)
    if existing:
        item_id = existing["id"]
        counts["existing"] += 1
        # Append only explicitly approved amendments. Never replace an edited
        # Project card's body, title, status, or other owner-maintained fields.
        amendment = task.get("projectAddendum")
        if amendment:
            marker = "<!-- PROJECT9_AMENDMENT_" + key.upper().replace("-", "_") + " -->"
            draft = (existing.get("content") or {})
            body = draft.get("body") or ""
            draft_id = draft.get("id")
            if marker not in body and draft_id:
                gql("""
                mutation($input:UpdateProjectV2DraftIssueInput!) {
                  updateProjectV2DraftIssue(input:$input) { draftIssue { id } }
                }
                """, input={"draftIssueId": draft_id, "body": body + "\n\n" + marker + "\n" + amendment})
                print("APPENDED APPROVED AMENDMENT", key, flush=True)
        print("PRESERVE DRAFT", key)
        continue  # Never overwrite hand-edited cards or their statuses on re-runs.

    result = gql("""
    mutation($input:AddProjectV2DraftIssueInput!) {
      addProjectV2DraftIssue(input:$input) { projectItem { id } }
    }
    """, input={"projectId": pid, "title": task["title"], "body": build_body(task)})
    item_id = result["addProjectV2DraftIssue"]["projectItem"]["id"]
    print("CREATED DRAFT", key, "item-id", item_id)
    counts["created"] += 1
    for field_name, field_value in [
        ("Status", task.get("status", "Концепция")),
        ("Релиз", task.get("release")),
        ("Направление", task.get("direction")),
        ("Порядок", task.get("order"))
    ]:
        update_field(item_id, field_name, field_value)
        counts["field_updates"] += 1
    time.sleep(0.15)

print("LEGACY ISSUE ITEMS STILL ON BOARD (check for duplicates):",
      [t["legacy"] for t in CFG["tasks"] if t.get("legacy") in old_issues])
print("SUMMARY", json.dumps(counts, ensure_ascii=False))
# Re-query with a bounded retry: the Projects API can lag behind new mutations.
verified = {}
missing = []
for attempt in range(4):
    _, check_items = read_project()
    verified = {}
    legacy_items = {}
    for item in check_items:
        c = item.get("content") or {}
        if c.get("__typename") == "DraftIssue":
            m = re.search(r"PROJECT9_CONCEPT_KEY:([a-z0-9-]+)", c.get("body") or "")
            if m:
                verified[m.group(1)] = item
        elif c.get("__typename") == "Issue":
            repo = (c.get("repository") or {}).get("nameWithOwner")
            if repo == CFG["owner"] + "/" + CFG["repo"]:
                legacy_items[c["number"]] = item
    missing = [t["key"] for t in CFG["tasks"] if t["key"] not in verified]
    for key in list(missing):
        direct = known_draft(key)
        if direct:
            verified[key] = direct[0]
            missing.remove(key)
            print("VERIFIED BY NODE", key, flush=True)
    if not missing:
        break
    print("Project read-back lag, attempt", attempt + 1, "missing", missing, flush=True)
    time.sleep(5)
if missing:
    raise RuntimeError("Drafts missing after sync: " + ", ".join(missing))

# Replace only old *closed* repo-Issue items that now have a verified Project-native
# Draft successor. Removing a Project item never deletes the underlying GitHub Issue.
for task in CFG["tasks"]:
    legacy = task.get("legacy")
    old_item = legacy_items.get(legacy)
    if not legacy or not old_item or task["key"] not in verified:
        continue
    c = old_item.get("content") or {}
    if c.get("state") != "CLOSED":
        print("PRESERVE active linked issue", legacy)
        continue
    gql("""
    mutation($input:DeleteProjectV2ItemInput!) {
      deleteProjectV2Item(input:$input) { deletedItemId }
    }
    """, input={"projectId": pid, "itemId": old_item["id"]})
    print("REPLACED closed linked Issue Project item", legacy, "with Draft", task["key"])

# Remove only duplicate native Drafts caused by earlier read-back indexing lag.
for task in CFG["tasks"]:
    matches = known_draft(task["key"])
    if len(matches) <= 1:
        continue
    keeper = verified[task["key"]]["id"]
    for extra in matches:
        if extra["id"] == keeper:
            continue
        gql("""
        mutation($input:DeleteProjectV2ItemInput!) {
          deleteProjectV2Item(input:$input) { deletedItemId }
        }
        """, input={"projectId": pid, "itemId": extra["id"]})
        print("REMOVED duplicate Draft", task["key"], extra["id"], flush=True)
print("VERIFIED", len(CFG["tasks"]), "native Project #9 draft cards", flush=True)
