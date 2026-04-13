/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aem.playground.core.services;

import java.util.ArrayList;
import java.util.List;

public class TaxonomyNode {
    private final String id;
    private final String name;
    private final String description;
    private final String parentId;
    private final List<TaxonomyNode> children;
    private final int depth;
    private final List<String> associatedContentIds;

    private TaxonomyNode(String id, String name, String description, String parentId,
                        List<TaxonomyNode> children, int depth, List<String> associatedContentIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.children = children;
        this.depth = depth;
        this.associatedContentIds = associatedContentIds;
    }

    public static TaxonomyNode create(String id, String name, String description, String parentId,
                                     List<TaxonomyNode> children, int depth, List<String> associatedContentIds) {
        return new TaxonomyNode(id, name, description, parentId, children, depth, associatedContentIds);
    }

    public static TaxonomyNode leaf(String id, String name) {
        return new TaxonomyNode(id, name, null, null, new ArrayList<>(), 0, new ArrayList<>());
    }

    public static TaxonomyNode root(String id, String name, String description) {
        return new TaxonomyNode(id, name, description, null, new ArrayList<>(), 0, new ArrayList<>());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getParentId() {
        return parentId;
    }

    public List<TaxonomyNode> getChildren() {
        return children;
    }

    public int getDepth() {
        return depth;
    }

    public List<String> getAssociatedContentIds() {
        return associatedContentIds;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public boolean isRoot() {
        return parentId == null;
    }

    public TaxonomyNode addChild(TaxonomyNode child) {
        if (children == null) {
            return this;
        }
        children.add(child);
        return this;
    }
}