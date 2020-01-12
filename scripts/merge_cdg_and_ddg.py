#!/usr/bin/env python3

import json
import sys

newNodeList = []
newEdgeList = []

AstIdToNewNodeId = {}
outputDoc = {}

for fileName in sys.argv[1:]:
    with open(fileName, 'r') as f:
        doc = json.load(f)

        for k,v in doc.items():
            if k not in {"nodes", "edges"}:
                outputDoc[k]=v

        nodeMapFromFileToMerged = {}

        # copy nodes and give new ids, then create map from old ids to merged new ids
        for node in doc["nodes"]:
            if "astId" in node:
                if node["astId"] in AstIdToNewNodeId:
                    nodeMapFromFileToMerged[int(node["id"])] = AstIdToNewNodeId[node["astId"]]
                    continue

            newId = len(newNodeList)
            nodeMapFromFileToMerged[int(node["id"])] = newId
            if "astId" in node:
                AstIdToNewNodeId[node["astId"]] = newId

            newNode = node.copy()
            newNode["id"] = newId
            newNodeList.append(newNode)

        # copy edges and give new ids
        for edge in doc["edges"]:
            newEdge = edge.copy()
            newEdge["source"] = nodeMapFromFileToMerged[edge["source"]]
            newEdge["target"] = nodeMapFromFileToMerged[edge["target"]]
            newEdge["id"] = len(newEdgeList)
            newEdgeList.append(newEdge)

outputDoc["type"] = "Program Dependence Graph (PDG)"
outputDoc["label"] = "PDG of " + outputDoc["file"]

outputDoc["nodes"] = newNodeList
outputDoc["edges"] = newEdgeList

json.dump(outputDoc, sys.stdout, indent=2, sort_keys=True)

