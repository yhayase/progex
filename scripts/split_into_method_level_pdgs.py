#!/usr/bin/env python3

import json
import sys
from collections import defaultdict

for arg in sys.argv[1:]:
    with open(arg, 'r') as f:
        doc = json.load(f)

        nodeList = {}
        entryPoints = []
        for node in doc["nodes"]:
            nodeList[node["id"]] = node
            if "entryPoint" in node and node["entryPoint"]:
                entryPoints.append(node)

        edgeList = {}
        edgeForward = defaultdict(list)
        for edge in doc["edges"]:
            edgeList[edge["id"]] = edge
            edgeForward[int(edge["source"])].append(edge)

        for entry in entryPoints:
            nodeQueue = [entry]
            nodesOfSubgraph = set()
            nodesOfSubgraph.add(entry["id"])
            edgesOfSubgraph = set()

            while len(nodeQueue)>0:
                currentNode = nodeQueue.pop(0)
                for oEdge in edgeForward[int(currentNode["id"])]:
                    if oEdge["id"] in edgesOfSubgraph:
                        continue
                    edgesOfSubgraph.add(oEdge["id"])

                    oTargetId = oEdge["target"]
                    if nodeList[oTargetId]["id"] in nodesOfSubgraph:
                        continue
                    nodesOfSubgraph.add(nodeList[oTargetId]["id"])
                    nodeQueue.append(nodeList[oTargetId])

            outputDoc = {}
            for k,v in doc.items():
                if k not in ["nodes", "edges"]:
                    outputDoc[k]=v
            if "name" in entry:
                 outputDoc["name"] = entry["name"]

            outputDoc["nodes"] = list(map((lambda id: nodeList[id]), nodesOfSubgraph))
            outputDoc["edges"] = list(map(lambda id: edgeList[id], edgesOfSubgraph))

            with open(arg+"."+str(entry["astId"]), 'w') as outputFile:
                json.dump(outputDoc, outputFile, indent=2)

