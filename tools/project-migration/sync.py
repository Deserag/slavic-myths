#!/usr/bin/env python3
"""Idempotent Slavic Myths Projects v2 migration, manually dispatched from Actions.
Requires GH_PROJECTS_TOKEN with 'project' scope; never alters repository Issues.
"""
import json
import os
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

CFG = json.loads(Path("tools/project-migration/config.json").read_text(encoding="utf-8"))
TOKEN = os.environ.get("GH_PROJECTS_TOKEN", "")
APPLY = os.environ.get("APPLY_MIGRATION") == "true"
OVERWRITE = os.environ.get("OVERWRITE_PROJECT_STATUS") == "true"
INCLUDE_CLOSED = os.environ.get("INCLUDE_CLOSED") != "false"
CONCEPTS_ONLY = os.environ.get("PROJECT_CONCEPTS_ONLY") == "true"
TARGET_ISSUES = set(CFG.get("projectConceptSyncIds", [])) if CONCEPTS_ONLY else set()
if CONCEPTS_ONLY and not TARGET_ISSUES:
    sys.exit("Concept-only sync requires explicit projectConceptSyncIds in config")
STATS = {"issues": 0, "to_add": 0, "existing": 0, "fields_changed": 0, "preserved": 0}
CHANGES = []
LEGACY = {"Todo": "Ожидание", "To do": "Ожидание",
          "In Progress": "В работе", "In progress": "В работе", "Done": "Готово"}
COLOR = {"Концепция": "GRAY", "Анализ": "BLUE", "Ожидание": "YELLOW",
         "В работе": "ORANGE", "Тестирование": "PURPLE", "Доработка": "RED",
         "Готово": "GREEN", "Отменена": "GRAY", "P0": "RED",
         "P1": "ORANGE", "P2": "BLUE"}
DESCRIPTION = {"Концепция": "Идея без ТЗ", "Анализ": "Обсуждение и подготовка",
               "Ожидание": "Готово к поручению Codex", "В работе": "Реализация",
               "Тестирование": "Ожидание игровой приёмки владельца",
               "Доработка": "Замечания после тестирования",
               "Готово": "Принято владельцем", "Отменена": "Отменено владельцем"}

if not TOKEN:
    sys.exit("Missing GH_PROJECTS_TOKEN. Set Actions secret SLAVIC_PROJECTS_TOKEN; never paste it into chat.")

def api(url, body=None):
    headers = {"Authorization": "Bearer " + TOKEN, "Accept": "application/vnd.github+json",
               "User-Agent": "SlavicMyths-Project-Migration", "X-GitHub-Api-Version": "2022-11-28"}
    data = json.dumps(body).encode("utf-8") if body is not None else None
    if data is not None:
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=data, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=45) as res:
            return json.loads(res.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        raise RuntimeError("GitHub HTTP %s: %s" % (e.code, e.read().decode("utf-8")[:600])) from e

def gql(query, **variables):
    response = api("https://api.github.com/graphql", {"query": query, "variables": variables})
    if response.get("errors"):
        raise RuntimeError("GraphQL errors: " + json.dumps(response["errors"], ensure_ascii=False)[:900])
    return response["data"]

def mutation(label, name, input_type, selection, values):
    CHANGES.append(label)
    if not APPLY:
        return {}
    query = ("mutation($input:" + input_type + "!){ " + name +
             "(input:$input){" + selection + "} }")
    result = gql(query, input=values)
    time.sleep(0.14)
    return result.get(name, {})

QUERY = """
query($owner:String!,$number:Int!,$repoOwner:String!,$repoName:String!) {
  user(login:$owner) { projectV2(number:$number) {
    id title readme public
    fields(first:100) { nodes {
      __typename
      ... on ProjectV2Field { id name dataType }
      ... on ProjectV2SingleSelectField { id name options { id name color description } }
    } }
    repositories(first:100) { nodes { id nameWithOwner } }
    views(first:60) { nodes { id name layout } }
  } }
  repository(owner:$repoOwner,name:$repoName) { id nameWithOwner }
}
"""
def fetch_project():
    data = gql(QUERY, owner=CFG["owner"], number=CFG["projectNumber"],
               repoOwner=CFG["owner"], repoName=CFG["repo"])
    project = (data.get("user") or {}).get("projectV2")
    repo = data.get("repository")
    if not project or not repo:
        raise RuntimeError("Project #9 or repository not available to the provided token")
    return project, repo

project, repository = fetch_project()
pid = project["id"]
print("Project:", project["title"], "| public:", project["public"], "| mode:", "APPLY" if APPLY else "DRY-RUN")

if not CONCEPTS_ONLY and (project["title"] != CFG["title"] or not project.get("readme")):
    entry = {"projectId": pid, "title": CFG["title"], "shortDescription": CFG["description"]}
    if not project.get("readme"):
        entry["readme"] = CFG["readme"]
    mutation("project metadata", "updateProjectV2", "UpdateProjectV2Input",
             "projectV2{id}", entry)

if not CONCEPTS_ONLY and repository["id"] not in [x["id"] for x in project["repositories"]["nodes"]]:
    mutation("link repository", "linkProjectV2ToRepository",
             "LinkProjectV2ToRepositoryInput", "repository{id}",
             {"projectId": pid, "repositoryId": repository["id"]})

def ensure_fields():
    fields = {x["name"]: x for x in project["fields"]["nodes"]}
    for name, settings in CFG["fields"].items():
        if CONCEPTS_ONLY and name != "Релиз":
            continue  # Only add the isolated Nether release option; do not touch Status or other fields.
        current = fields.get(name)
        if settings["type"] == "NUMBER":
            if not current:
                mutation("create number field " + name, "createProjectV2Field",
                         "CreateProjectV2FieldInput",
                         "projectV2Field{... on ProjectV2Field{id}}",
                         {"projectId": pid, "name": name, "dataType": "NUMBER"})
            elif current["__typename"] != "ProjectV2Field":
                raise RuntimeError("Incompatible existing number field: " + name)
            continue
        if current and current["__typename"] != "ProjectV2SingleSelectField":
            raise RuntimeError("Incompatible existing single-select field: " + name)
        old_options = current.get("options", []) if current else []
        original = {x["name"] for x in old_options}
        options = []
        present = set()
        for old in old_options:
            target = (LEGACY.get(old["name"], old["name"])
                      if name == "Status" and LEGACY.get(old["name"]) not in original
                      else old["name"])
            option = {"id": old["id"], "name": target, "color": COLOR.get(target, old["color"]),
                      "description": DESCRIPTION.get(target, old.get("description", ""))}
            options.append(option)
            present.add(target)
        for target in settings["values"]:
            if target not in present:
                options.append({"name": target, "color": COLOR.get(target, "BLUE"),
                                "description": DESCRIPTION.get(target, "")})
                present.add(target)
        if not current:
            mutation("create field " + name, "createProjectV2Field",
                     "CreateProjectV2FieldInput",
                     "projectV2Field{... on ProjectV2SingleSelectField{id}}",
                     {"projectId": pid, "name": name, "dataType": "SINGLE_SELECT",
                      "singleSelectOptions": options})
        elif len(options) != len(old_options) or any(
            a["name"] != b["name"] for a, b in zip(options, old_options)
        ):
            mutation("add/rename options " + name, "updateProjectV2Field",
                     "UpdateProjectV2FieldInput",
                     "projectV2Field{... on ProjectV2SingleSelectField{id}}",
                     {"fieldId": current["id"], "singleSelectOptions": options})

ensure_fields()  # Concept sync limits field maintenance to the Релиз select.
if APPLY:
    project, repository = fetch_project()
fields = {x["name"]: x for x in project["fields"]["nodes"]}

if not CONCEPTS_ONLY:
    views = {x["name"]: x for x in project["views"]["nodes"]}
    for view in CFG["views"]:
        if view["name"] in views:
            continue
        generic = next((v for v in views.values()
                        if re.fullmatch(r"(View|Table) [0-9]+", v["name"], re.I)), None)
        if view["name"] == "Работа" and generic:
            mutation("rename default view", "updateProjectV2View",
                     "UpdateProjectV2ViewInput", "projectV2View{id}",
                     {"viewId": generic["id"], "name": "Работа", "layout": view["layout"]})
            views.pop(generic["name"])
            views["Работа"] = dict(generic, name="Работа")
        else:
            result = mutation("create view " + view["name"], "createProjectV2View",
                              "CreateProjectV2ViewInput", "projectV2View{id name}",
                              {"projectId": pid, "name": view["name"], "layout": view["layout"]})
            if APPLY:
                views[view["name"]] = result["projectV2View"]
    for view in CFG["views"]:
        if view.get("filter") and view["name"] in views:
            mutation("set view filter " + view["name"], "updateProjectV2View",
                     "UpdateProjectV2ViewInput", "projectV2View{id}",
                     {"viewId": views[view["name"]]["id"], "filter": view["filter"]})

def all_issues():
    page = 1
    while True:
        path = "/repos/%s/%s/issues?state=all&per_page=100&page=%d" % (
            CFG["owner"], CFG["repo"], page)
        result = api("https://api.github.com" + path)
        for entry in result:
            if "pull_request" not in entry and (INCLUDE_CLOSED or entry["state"] == "open"):
                yield entry
        if len(result) < 100:
            break
        page += 1

overrides = {}
for status, nums in CFG["statusOverrides"].items():
    for number in nums:
        if number in overrides:
            raise RuntimeError("Issue has two planned statuses: #%d" % number)
        overrides[number] = status

def status(issue):
    n = issue["number"]
    if n in overrides:
        return overrides[n]
    if issue["state"] == "open":
        return "Анализ"  # no assumed implementation
    if issue.get("state_reason") in ("not_planned", "duplicate"):
        return "Отменена"
    if "owner-accepted" in [
        x if isinstance(x, str) else x["name"] for x in issue.get("labels", [])
    ]:
        return "Готово"
    return "Анализ"  # legacy closed is not automatically accepted

def priority(issue):
    if str(issue["number"]) in CFG["priorityOverrides"]:
        return CFG["priorityOverrides"][str(issue["number"])]
    match = re.search(r"\[(P[012])\]", issue["title"], re.I)
    return match.group(1).upper() if match else ("P2" if issue["state"] == "open" else None)

def release(issue):
    n = str(issue["number"])
    if n in CFG["releaseOverrides"]:
        return CFG["releaseOverrides"][n]
    t = issue["title"]
    if re.search(r"Classes 3\.0|\[3\.0\]", t, re.I):
        return "3.0"
    if re.search(r"\[2\.1\]|Nether.*2\.1", t, re.I):
        return "2.1"
    if re.search(r"\[2\.x\]|\[HOLD\]", t, re.I):
        return "Будущее"
    return "2.0"

GROUPS = {
    "Руны": [86, 102, 103, 104, 105],
    "Оружие": [87, 115, 116, 117, 118, 119, 120, 121, 3, 4, 5, 6, 7, 8, 9, 10, 26],
    "Броня и одежда": [88, 122, 123, 124, 125, 126, 127, 128, 129, 130],
    "Арт предметов": [81,82,83,84,85,91,92,95,96,97,98,99,100,101,
                       106,107,108,109,110,111,112,113,114],
    "Существа": [76,77,79,80,93,48,49,50,51,53,54,55,56,57,58,67,68,69,70,71],
    "Аудио": [59,60,61,62,63,64,65,66],
    "Производительность и QA": [28,33,34,39],
    "Интерфейс": [72,73,74,75,90]
}
def direction(issue):
    n = issue["number"]
    explicit = CFG.get("directionOverrides", {}).get(str(n))
    if explicit:
        return explicit
    for name, nums in GROUPS.items():
        if n in nums:
            return name
    t = issue["title"].lower()
    for pattern, name in [
        (r"генерац|незер|биом|worldgen|структур", "Мир и генерация"),
        (r"аудио|audio|звук|sound", "Аудио"),
        (r"анимац|существ|моб|волк|ским|лихо|тугар", "Существа"),
        (r"hud|gui|класс|команд|интерфейс", "Интерфейс")
    ]:
        if re.search(pattern, t):
            return name
    return "Механики"

SNAPSHOT = """
query($id:ID!,$after:String) {
  node(id:$id) { ... on ProjectV2 {
    items(first:100,after:$after) {
      pageInfo { hasNextPage endCursor }
      nodes {
        id
        content { ... on Issue { number repository { nameWithOwner } } }
        fieldValues(first:30) { nodes {
          ... on ProjectV2ItemFieldSingleSelectValue {
            name field { ... on ProjectV2FieldCommon { name } }
          }
          ... on ProjectV2ItemFieldNumberValue {
            number field { ... on ProjectV2FieldCommon { name } }
          }
        } }
      }
    }
  } }
}
"""
existing = {}
cursor = None
while True:
    batch = gql(SNAPSHOT, id=pid, after=cursor)["node"]["items"]
    for item in batch["nodes"]:
        c = item.get("content")
        if not c or c.get("repository", {}).get("nameWithOwner") != CFG["owner"] + "/" + CFG["repo"]:
            continue
        values = {}
        for field in item["fieldValues"]["nodes"]:
            if field and field.get("field"):
                values[field["field"]["name"]] = field.get("number", field.get("name"))
        existing[c["number"]] = (item["id"], values)
    if not batch["pageInfo"]["hasNextPage"]:
        break
    cursor = batch["pageInfo"]["endCursor"]

issues = sorted([issue for issue in all_issues() if not TARGET_ISSUES or issue["number"] in TARGET_ISSUES], key=lambda x: x["number"])
if CONCEPTS_ONLY and {issue["number"] for issue in issues} != TARGET_ISSUES:
    raise RuntimeError("Concept sync issue list does not match actual repository Issues")
if CONCEPTS_ONLY and any(overrides.get(n) != "Концепция" for n in TARGET_ISSUES):
    raise RuntimeError("Concept sync includes Issue without approved Концепция override")
STATS["issues"] = len(issues)
desired = {}
for issue in issues:
    n = issue["number"]
    desired[n] = {
        "Status": status(issue),
        "Priority": priority(issue),
        "Релиз": release(issue),
        "Направление": direction(issue),
        "Порядок": CFG["orders"].get(str(n))
    }
    if n in existing:
        STATS["existing"] += 1
    else:
        STATS["to_add"] += 1

for issue in issues:
    n = issue["number"]
    item_id, values = existing.get(n, (None, {}))
    if item_id is None:
        result = mutation("add Issue #%d" % n, "addProjectV2ItemById",
                          "AddProjectV2ItemByIdInput", "item{id}",
                          {"projectId": pid, "contentId": issue["node_id"]})
        if APPLY:
            item_id = result["item"]["id"]
    for name, value in desired[n].items():
        if value is None:
            continue
        old_value = values.get(name)
        if old_value == value:
            STATS["preserved"] += 1
            continue
        if name == "Status" and not OVERWRITE and not CONCEPTS_ONLY and old_value is not None and old_value not in LEGACY:
            STATS["preserved"] += 1
            continue
        field = fields.get(name)
        if APPLY and not field:
            raise RuntimeError("Missing Project field " + name)
        if name == "Порядок":
            entry = {"number": value}
        else:
            option = next((o for o in (field or {}).get("options", [])
                           if o["name"] == value), None)
            if APPLY and not option:
                raise RuntimeError("Missing option %s=%s" % (name, value))
            entry = {"singleSelectOptionId": option["id"]} if option else {}
        if APPLY:
            mutation("Issue #%d: %s=%s" % (n, name, value),
                     "updateProjectV2ItemFieldValue", "UpdateProjectV2ItemFieldValueInput",
                     "projectV2Item{id}", {
                         "projectId": pid, "itemId": item_id, "fieldId": field["id"],
                         "value": entry
                     })
        STATS["fields_changed"] += 1

print("Summary:", json.dumps(STATS, ensure_ascii=False))
if not APPLY:
    print("Dry run, no changes. Planned actions:", len(CHANGES))
    for action in CHANGES[:35]:
        print(" -", action)
summary = os.environ.get("GITHUB_STEP_SUMMARY")
if summary:
    with open(summary, "a", encoding="utf-8") as out:
        out.write("## Project #9 migration (%s)\n\n" % ("APPLY" if APPLY else "DRY RUN"))
        out.write("| Property | Count |\n|---|---:|\n")
        for key, val in STATS.items():
            out.write("| %s | %s |\n" % (key, val))
        out.write("\nExisting manually assigned canonical statuses are preserved unless overwrite is enabled.\n")
