#!/usr/bin/env python3
"""Replace Project #9 Draft placeholders with corresponding real GitHub Issues.

Issues are created beforehand via the connected GitHub user's Issue permissions.
The Actions Project token has Projects write access, but cannot convert Drafts
in-place. This tool links the existing Issues and copies all supported project
fields from the Draft before removing it from the board. Never deletes Issues.
Idempotent: a repeat run uses existing project-linked Issues.
"""
import json
import os
import re
import time
import urllib.request
import urllib.error
from pathlib import Path

cfg=json.loads(Path("tools/project-migration/converted-project9-issues.json").read_text("utf-8"))
token=os.getenv("GH_PROJECTS_TOKEN")
if not token: raise RuntimeError("Missing Project secret")
url="https://api.github.com/graphql"

def gql(query,**v):
    req=urllib.request.Request(url,data=json.dumps({"query":query,"variables":v}).encode("utf-8"),headers={"Authorization":"Bearer "+token,"Accept":"application/vnd.github+json","Content-Type":"application/json","X-GitHub-Api-Version":"2022-11-28"})
    try:
        with urllib.request.urlopen(req,timeout=50) as r: data=json.load(r)
    except urllib.error.HTTPError as e:
        raise RuntimeError("HTTP "+str(e.code)+" "+e.read().decode("utf-8")[:400]) from e
    if data.get("errors"): raise RuntimeError("GraphQL "+json.dumps(data["errors"],ensure_ascii=False)[:1000])
    return data["data"]

Q="""query($owner:String!,$number:Int!,$cursor:String) {
user(login:$owner) { projectV2(number:$number) {
id title fields(first:100) { nodes {
... on ProjectV2SingleSelectField { id name options {id name} }
... on ProjectV2Field {id name dataType}
} }
items(first:100,after:$cursor) { pageInfo {hasNextPage endCursor}
nodes { id
content {__typename ... on DraftIssue{id title body} ... on Issue{id number title body repository{nameWithOwner} url}}
fieldValues(first:40){nodes{
... on ProjectV2ItemFieldSingleSelectValue {
name field { ... on ProjectV2SingleSelectField {name} }
}
... on ProjectV2ItemFieldNumberValue {
number field {... on ProjectV2Field{name} }
}
}}
}
} } } }
"""
ISSUE_Q="""query($owner:String!,$repo:String!,$number:Int!){
repository(owner:$owner,name:$repo){issue(number:$number){id number title url}}
}"""
ADD="""mutation($project:ID!,$issue:ID!){
addProjectV2ItemById(input:{projectId:$project,contentId:$issue}){item{id}}
}"""
UPDATE="""mutation($project:ID!,$item:ID!,$field:ID!,$option:String!){
updateProjectV2ItemFieldValue(input:{projectId:$project,itemId:$item,fieldId:$field,value:{singleSelectOptionId:$option}}){projectV2Item{id}}
}"""
UPDATE_NUMBER="""mutation($project:ID!,$item:ID!,$field:ID!,$value:Float!){
updateProjectV2ItemFieldValue(input:{projectId:$project,itemId:$item,fieldId:$field,value:{number:$value}}){projectV2Item{id}}
}"""
DELETE="""mutation($project:ID!,$item:ID!){
deleteProjectV2Item(input:{projectId:$project,itemId:$item}){deletedItemId}
}"""
NODE_Q="""query($id:ID!){ node(id:$id){... on ProjectV2Item {
 id fieldValues(first:40){nodes{
 ... on ProjectV2ItemFieldSingleSelectValue {name field {... on ProjectV2SingleSelectField{name}}}
 ... on ProjectV2ItemFieldNumberValue {number field {... on ProjectV2Field{name}}}
 }}
}}}"""

def read_project():
    cursor=None; items=[]; project=None
    while True:
        z=gql(Q,owner=cfg["projectOwner"],number=cfg["projectNumber"],cursor=cursor)
        project=(z.get("user") or {}).get("projectV2")
        if not project:raise RuntimeError("Cannot open Project #9")
        items.extend(project["items"]["nodes"])
        info=project["items"]["pageInfo"]
        if not info["hasNextPage"]:break
        cursor=info["endCursor"]
    return project,items

def values(item):
    out={}
    for v in (item.get("fieldValues") or {}).get("nodes",[]):
        if not v:continue
        name=(v.get("field") or {}).get("name")
        if not name:continue
        if "name" in v and v["name"] is not None:out[name]=("select",v["name"])
        elif v.get("number") is not None:out[name]=("number",v["number"])
    return out

def update_values(project,item_id,old_values):
    fields={x["name"]:x for x in project["fields"]["nodes"] if x and x.get("name")}
    for name,(kind,value) in old_values.items():
        field=fields.get(name)
        if not field:continue
        if kind=="select":
            opts=field.get("options",[])
            option=next((x for x in opts if x["name"]==value),None)
            if option:gql(UPDATE,project=project["id"],item=item_id,field=field["id"],option=option["id"])
        elif kind=="number":
            gql(UPDATE_NUMBER,project=project["id"],item=item_id,field=field["id"],value=float(value))

def get_item_values(item_id):
    z=gql(NODE_Q,id=item_id)
    item=z.get("node")
    return values(item) if item else None

project,items=read_project()
pid=project["id"]; repo_name=cfg["projectOwner"]+"/"+cfg["repo"]
drafts={};issues={}
for item in items:
    c=item.get("content") or {}
    if c.get("__typename")=="DraftIssue":
        m=re.search(r"PROJECT9_CONCEPT_KEY:([a-z0-9-]+)",c.get("body") or "")
        if m and m.group(1) in cfg["issues"]:
            if m.group(1) in drafts:raise RuntimeError("Duplicate Draft key: "+m.group(1))
            drafts[m.group(1)]=item
    if c.get("__typename")=="Issue" and (c.get("repository") or {}).get("nameWithOwner")==repo_name:
        issues[c["number"]]=item

print("PROJECT",project["title"],"drafts",len(drafts),"already linked",len(issues),"migrating",len(cfg["issues"]),flush=True)
for key,number in cfg["issues"].items():
    original=drafts.get(key)
    existing=issues.get(number)
    if not original:
        if existing:
            print("ALREADY MIGRATED",key,"Issue #"+str(number),flush=True);continue
        raise RuntimeError("Draft and linked Issue both absent for "+key)

    old_values=values(original)
    old_status=old_values.get("Status")
    if not old_status:raise RuntimeError("Draft status missing "+key)
    if not existing:
        q=gql(ISSUE_Q,owner=cfg["projectOwner"],repo=cfg["repo"],number=int(number))
        target=((q.get("repository") or {}).get("issue") or {})
        if not target.get("id"):raise RuntimeError("Issue not found #"+str(number))
        result=gql(ADD,project=pid,issue=target["id"])
        item_id=result["addProjectV2ItemById"]["item"]["id"]
        print("LINKED",key,"#"+str(number),item_id,flush=True)
    else:
        item_id=existing["id"]
        print("EXISTING LINK",key,"#"+str(number),flush=True)

    update_values(project,item_id,old_values)
    after=get_item_values(item_id)
    if not after or after.get("Status")!=old_status:
        raise RuntimeError("Refusing to remove Draft: Status mismatch "+key+" before="+str(old_status)+" after="+str(after))
    # Check additional specified metadata before removing old project card.
    for field in ("Релиз","Направление","Порядок"):
        if field in old_values and after.get(field)!=old_values[field]:
            raise RuntimeError("Refusing to remove Draft: metadata mismatch "+key+" "+field)
    gql(DELETE,project=pid,item=original["id"])
    print("MIGRATED",key,"Issue #"+str(number),"Status",old_status[1],flush=True)

final_project,final=read_project()
final_issues={it["content"]["number"]:it for it in final if (it.get("content") or {}).get("__typename")=="Issue" and ((it["content"].get("repository") or {}).get("nameWithOwner")==repo_name)}
still_draft=set()
for it in final:
    c=it.get("content") or {}
    if c.get("__typename")=="DraftIssue":
        m=re.search(r"PROJECT9_CONCEPT_KEY:([a-z0-9-]+)",c.get("body") or "")
        if m and m.group(1) in cfg["issues"]:still_draft.add(m.group(1))
missing=[key for key,number in cfg["issues"].items() if number not in final_issues]
if missing or still_draft:raise RuntimeError("MISSING issues "+str(missing)+"; remaining Drafts "+str(still_draft))
print("SUCCESS: all",len(cfg["issues"]),"working tasks are numbered GitHub Issues linked to Project #9; no corresponding Drafts remain",flush=True)
