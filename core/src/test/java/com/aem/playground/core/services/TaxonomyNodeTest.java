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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaxonomyNodeTest {

    @Test
    void testCreateWithAllFields() {
        List<TaxonomyNode> children = Arrays.asList(
            TaxonomyNode.leaf("child1", "Child One"),
            TaxonomyNode.leaf("child2", "Child Two")
        );
        List<String> contentIds = Arrays.asList("content-1", "content-2");

        TaxonomyNode node = TaxonomyNode.create("parent-1", "Parent Node", "Description", null, children, 1, contentIds);

        assertEquals("parent-1", node.getId());
        assertEquals("Parent Node", node.getName());
        assertEquals("Description", node.getDescription());
        assertNull(node.getParentId());
        assertEquals(1, node.getDepth());
        assertEquals(contentIds, node.getAssociatedContentIds());
    }

    @Test
    void testLeaf() {
        TaxonomyNode leaf = TaxonomyNode.leaf("leaf-1", "Leaf Node");

        assertEquals("leaf-1", leaf.getId());
        assertEquals("Leaf Node", leaf.getName());
        assertNull(leaf.getParentId());
        assertTrue(leaf.getChildren().isEmpty());
        assertEquals(0, leaf.getDepth());
    }

    @Test
    void testRoot() {
        TaxonomyNode root = TaxonomyNode.root("root", "Taxonomy Root", "Root description");

        assertEquals("root", root.getId());
        assertEquals("Taxonomy Root", root.getName());
        assertEquals("Root description", root.getDescription());
        assertNull(root.getParentId());
        assertTrue(root.isRoot());
    }

    @Test
    void testHasChildren() {
        TaxonomyNode parent = TaxonomyNode.create("parent", "Parent", "desc", null, 
            Arrays.asList(TaxonomyNode.leaf("child", "Child")), 0, null);
        assertTrue(parent.hasChildren());

        TaxonomyNode leaf = TaxonomyNode.leaf("leaf", "Leaf");
        assertFalse(leaf.hasChildren());
    }

    @Test
    void testHasChildrenWithEmptyChildrenList() {
        TaxonomyNode node = TaxonomyNode.leaf("node", "Node");
        assertFalse(node.hasChildren());
    }

    @Test
    void testIsRoot() {
        TaxonomyNode root = TaxonomyNode.root("root", "Root", null);
        assertTrue(root.isRoot());

        TaxonomyNode child = TaxonomyNode.create("child", "Child", null, "parent-id", new ArrayList<>(), 1, null);
        assertFalse(child.isRoot());
    }

    @Test
    void testAddChild() {
        TaxonomyNode parent = TaxonomyNode.root("parent", "Parent", null);
        TaxonomyNode child = TaxonomyNode.leaf("child", "Child");

        TaxonomyNode result = parent.addChild(child);

        assertSame(parent, result);
        assertEquals(1, parent.getChildren().size());
    }

    @Test
    void testAddChildWithNullChildren() {
        TaxonomyNode node = TaxonomyNode.leaf("leaf", "Leaf");
        TaxonomyNode newChild = TaxonomyNode.leaf("new", "New");

        TaxonomyNode result = node.addChild(newChild);

        assertSame(node, result);
    }

    @Test
    void testGetters() {
        TaxonomyNode node = TaxonomyNode.create("test-id", "Test Name", "Test desc", "parent-1", 
            new ArrayList<>(), 2, new ArrayList<>());

        assertEquals("test-id", node.getId());
        assertEquals("Test Name", node.getName());
        assertEquals("Test desc", node.getDescription());
        assertEquals("parent-1", node.getParentId());
        assertEquals(2, node.getDepth());
    }
}