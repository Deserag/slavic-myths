#!/usr/bin/env python3
"""Idempotent, scope-limited synchronization of Nether biome planning Issues with Project #9.

Only add listed Issue cards. Upgrade listed cards from Concept to Analysis where
approved; never downgrade In Progress/Testing/Done or rewrite unrelated fields.
Designed for the pre-existing SLAVIC_PROJECTS_TOKEN GitHub Actions secret.
No Minecraft code or gameplay changes.
"""
import json
import os
import urllib.request

TOKEN = os.getenv("GH_PROJECTS_TOKEN")
if not TOKEN:
    raise SystemExit("Missing SLAVIC_PROJECTS_TOKEN; no changes made")

OWNER = "Deserag"
REPO = "slavic-myths"
PROJECT_NUMBER = 9
# Agreed planning Issues for 2.0.2, 2.0.3 and 2.0.4 (including unapproved future mob/boss concepts).
TARGETS = {
    250: ("Анализ", None),
    251: ("Анализ", None),
    252: ("Анализ", None),
    253: ("Анализ", None),
    254: ("Анализ", None),
    255: ("Анализ", None),
    256: ("Анализ", None),
    257: ("Анализ", None),
    261: ("Анализ", 2030),
    262: ("Анализ", 2031),
    263: ("Анализ", 2032),
    264: ("Анализ", 2033),
    265: ("Анализ", 2034),
    266: ("В работе", 2035),
    267: ("Анализ", 2036),
    288: ("Анализ", 2040),
    289: ("Анализ", 2041),
    290: ("Анализ", 2042),
    291: ("Анализ", 2043),
    292: ("Ожидание", 2044),
    293: ("Ожидание", 2045),
    294: ("Анализ", 2046),
    301: ("Ожидание", 20445),
    302: ("Ожидание", 20455),
    303: ("Ожидание", 20454),
}
GRAPHQL = "https://api.github.com/graphql"

def gql(query, **variables):
    payload = json.dumps({"query": query, "variables": variables}, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(
        GRAPHQL, data=payload,
        headers={"Authorization": "Bearer " + TOKEN,
                 "Accept": "application/vnd.github+json",
                 "Content-Type": "application/json",
                 "X-GitHub-Api-Version": "2022-11-28",
                 "User-Agent": "SlavicMyths-Project9-BiomePlanning"},
    )
    with urllib.request.urlopen(req, timeout=60) as response:
        result = json.load(response)
    if result.get("errors"):
        raise RuntimeError("GraphQL response errors: " + json.dumps(result["errors"], ensure_ascii=False)[:1000])
    return result["data"]

PROJECT_QUERY = """
query($owner:String!, $number:Int!, $after:String) {
  user(login:$owner) {
    projectV2(number:$number) {
      id title
      fields(first:100) { nodes {
        ... on ProjectV2SingleSelectField { id name options { id name } }
        ... on ProjectV2Field { id name dataType }
      }}
      items(first:100,after:$after) {
        pageInfo { hasNextPage endCursor }
        nodes {
          id
          content { __typename ... on Issue { id number repository { nameWithOwner } } }
        }
      }
    }
  }
}"""
ISSUE_QUERY = """
query($owner:String!, $repo:String!, $number:Int!) {
  repository(owner:$owner,name:$repo) { issue(number:$number) { id number } }
}"""
ADD_QUERY = """
mutation($project:ID!, $issue:ID!) {
  addProjectV2ItemById(input:{projectId:$project, contentId:$issue}) { item { id } }
}"""
SET_QUERY = """
mutation($input:UpdateProjectV2ItemFieldValueInput!) {
  updateProjectV2ItemFieldValue(input:$input) { projectV2Item { id } }
}"""
ITEM_QUERY = """
query($id:ID!) {
  node(id:$id) { ... on ProjectV2Item {
    id
    fieldValues(first:50) { nodes {
      ... on ProjectV2ItemFieldSingleSelectValue {
        name field { ... on ProjectV2SingleSelectField { name } }
      }
      ... on ProjectV2ItemFieldNumberValue {
        number field { ... on ProjectV2Field { name } }
      }
    }}
  }}
}"""

def get_project():
    cursor = None
    items = []
    while True:
        result = gql(PROJECT_QUERY, owner=OWNER, number=PROJECT_NUMBER, after=cursor)
        project = (result.get("user") or {}).get("projectV2")
        if not project:
            raise RuntimeError("Project #9 not accessible to token")
        page = project["items"]
        items.extend(page["nodes"])
        if not page["pageInfo"]["hasNextPage"]:
            return project, items
        cursor = page["pageInfo"]["endCursor"]

def field_values(item_id):
    item = gql(ITEM_QUERY, id=item_id).get("node")
    if not item:
        raise RuntimeError("Cannot read Project item " + item_id)
    result = {}
    for v in item["fieldValues"]["nodes"]:
        if not v:
            continue
        name = (v.get("field") or {}).get("name")
        if name and v.get("name") is not None:
            result[name] = v["name"]
        elif name and v.get("number") is not None:
            result[name] = v["number"]
    return result

def set_field(pid, item_id, fields, name, value):
    f = fields.get(name)
    if not f:
        print("SKIP missing Project field", name, flush=True)
        return
    if name == "Порядок":
        val = {"number": float(value)}
    else:
        opt = next((o for o in f.get("options", []) if o["name"] == value), None)
        if not opt:
            raise RuntimeError("Project field " + name + " lacks option " + str(value))
        val = {"singleSelectOptionId": opt["id"]}
    gql(SET_QUERY, input={
        "projectId": pid, "itemId": item_id,
        "fieldId": f["id"], "value": val
    })

project, all_items = get_project()
pid = project["id"]
fields = {f["name"]: f for f in project["fields"]["nodes"] if f and f.get("name")}
status_names = {o["name"] for o in fields["Status"]["options"]}
if not {"Концепция", "Анализ"}.issubset(status_names):
    raise RuntimeError("Project status options do not match expected Концепция / Анализ")
linked = {}
for item in all_items:
    c = item.get("content") or {}
    if c.get("__typename") == "Issue" and (c.get("repository") or {}).get("nameWithOwner") == OWNER + "/" + REPO:
        linked[c["number"]] = item["id"]

print("PROJECT", project["title"], "linked-existing", len(linked), flush=True)
for number, (target_status, order) in TARGETS.items():
    item_id = linked.get(number)
    created = not bool(item_id)
    if created:
        issue = (gql(ISSUE_QUERY, owner=OWNER, repo=REPO, number=number).get("repository") or {}).get("issue")
        if not issue:
            raise RuntimeError("Issue not found: #" + str(number))
        item_id = gql(ADD_QUERY, project=pid, issue=issue["id"])["addProjectV2ItemById"]["item"]["id"]
    values = field_values(item_id)
    status = values.get("Status")
    # Promote only explicit owner-approved transitions; never demote progressed work.
    allowed = status is None or (
        status == "Концепция" and target_status in ("Анализ", "Ожидание", "В работе")
    ) or (
        status == "Анализ" and target_status in ("Ожидание", "В работе")
    )
    if allowed:
        set_field(pid, item_id, fields, "Status", target_status)
    elif status != target_status:
        print("PRESERVE advanced/current status", number, status, flush=True)
    if order is not None:
        if "Релиз" not in values:
            set_field(pid, item_id, fields, "Релиз", "2.0 · Ад")
        if "Направление" not in values:
            set_field(pid, item_id, fields, "Направление", "Мир и генерация")
        if "Порядок" not in values:
            set_field(pid, item_id, fields, "Порядок", order)
    now = field_values(item_id).get("Status")
    if target_status == "Анализ" and now not in ("Анализ", "Ожидание", "В работе", "Тестирование", "Доработка", "Готово"):
        raise RuntimeError("Status verification failed #" + str(number) + ": " + str(now))
    if target_status == "Ожидание" and now not in ("Ожидание", "В работе", "Тестирование", "Доработка", "Готово"):
        raise RuntimeError("Awaiting owner-approved implementation #" + str(number) + ": " + str(now))
    if target_status == "В работе" and now not in ("В работе", "Тестирование", "Доработка", "Готово"):
        raise RuntimeError("In-progress status verification failed #" + str(number) + ": " + str(now))
    if target_status == "Концепция" and now is None:
        raise RuntimeError("Concept status unset #" + str(number))
    print(("ADDED" if created else "UPDATED"), "#" + str(number),
          "status", now, "order", order, flush=True)
print("SUCCESS - Project #9 synchronized; no other Issues or statuses modified.")
