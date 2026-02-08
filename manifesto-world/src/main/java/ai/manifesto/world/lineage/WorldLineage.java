package ai.manifesto.world.lineage;

import ai.manifesto.world.schema.DecisionId;
import ai.manifesto.world.schema.EdgeId;
import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.World;
import ai.manifesto.world.schema.WorldEdge;
import ai.manifesto.world.schema.WorldId;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class WorldLineage {
    private final Map<String, World> worlds = new LinkedHashMap<>();
    private final Map<String, WorldEdge> edges = new LinkedHashMap<>();
    private final Map<String, String> parentEdgeByChild = new HashMap<>();
    private final Map<String, Set<String>> childEdgeIdsByParent = new HashMap<>();

    private WorldId genesisId;

    public void setGenesis(World world) {
        Objects.requireNonNull(world, "world is required");
        if (genesisId != null) {
            throw new IllegalStateException("Genesis world already exists: " + genesisId.value());
        }
        if (world.getCreatedBy() != null) {
            throw new IllegalArgumentException("Genesis world must have createdBy == null");
        }
        worlds.put(world.getWorldId().value(), world);
        genesisId = world.getWorldId();
    }

    public boolean hasGenesis() {
        return genesisId != null;
    }

    public World getGenesis() {
        if (genesisId == null) {
            return null;
        }
        return worlds.get(genesisId.value());
    }

    public WorldId getGenesisId() {
        return genesisId;
    }

    public void addWorld(World world) {
        Objects.requireNonNull(world, "world is required");
        String worldId = world.getWorldId().value();
        if (worlds.containsKey(worldId)) {
            throw new IllegalArgumentException("World already exists in lineage: " + worldId);
        }
        worlds.put(worldId, world);
    }

    public WorldEdge addWorldWithEdge(World world, WorldId parentId, ProposalId proposalId, DecisionId decisionId, long createdAt) {
        Objects.requireNonNull(world, "world is required");
        Objects.requireNonNull(parentId, "parentId is required");
        Objects.requireNonNull(proposalId, "proposalId is required");
        Objects.requireNonNull(decisionId, "decisionId is required");

        String childId = world.getWorldId().value();
        if (!worlds.containsKey(parentId.value())) {
            throw new IllegalArgumentException("Parent world not found: " + parentId.value());
        }
        if (worlds.containsKey(childId)) {
            throw new IllegalArgumentException("World already exists in lineage: " + childId);
        }
        if (wouldCreateCycle(parentId, world.getWorldId())) {
            throw new IllegalStateException("Adding this edge would create a cycle");
        }

        worlds.put(childId, world);

        EdgeId edgeId = EdgeId.of("edge-" + UUID.randomUUID());
        WorldEdge edge = new WorldEdge(edgeId, parentId, world.getWorldId(), proposalId, decisionId, createdAt);
        edges.put(edgeId.value(), edge);
        parentEdgeByChild.put(childId, edgeId.value());
        childEdgeIdsByParent.computeIfAbsent(parentId.value(), ignored -> new HashSet<>()).add(edgeId.value());
        return edge;
    }

    public World getWorld(WorldId worldId) {
        return worlds.get(worldId.value());
    }

    public boolean hasWorld(WorldId worldId) {
        return worlds.containsKey(worldId.value());
    }

    public List<World> getAllWorlds() {
        return new ArrayList<>(worlds.values());
    }

    public int getWorldCount() {
        return worlds.size();
    }

    public WorldEdge getEdge(EdgeId edgeId) {
        return edges.get(edgeId.value());
    }

    public List<WorldEdge> getAllEdges() {
        return new ArrayList<>(edges.values());
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public WorldEdge getParentEdge(WorldId worldId) {
        String edgeId = parentEdgeByChild.get(worldId.value());
        return edgeId == null ? null : edges.get(edgeId);
    }

    public World getParent(WorldId worldId) {
        WorldEdge edge = getParentEdge(worldId);
        return edge == null ? null : worlds.get(edge.getFromWorld().value());
    }

    public List<WorldEdge> getChildrenEdges(WorldId worldId) {
        Set<String> ids = childEdgeIdsByParent.get(worldId.value());
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<WorldEdge> results = new ArrayList<>();
        for (String id : ids) {
            WorldEdge edge = edges.get(id);
            if (edge != null) {
                results.add(edge);
            }
        }
        return results;
    }

    public List<World> getChildren(WorldId worldId) {
        List<World> results = new ArrayList<>();
        for (WorldEdge edge : getChildrenEdges(worldId)) {
            World child = worlds.get(edge.getToWorld().value());
            if (child != null) {
                results.add(child);
            }
        }
        return results;
    }

    public boolean hasChildren(WorldId worldId) {
        Set<String> ids = childEdgeIdsByParent.get(worldId.value());
        return ids != null && !ids.isEmpty();
    }

    public boolean isLeaf(WorldId worldId) {
        return !hasChildren(worldId);
    }

    public List<World> getAncestors(WorldId worldId) {
        List<World> ancestors = new ArrayList<>();
        World current = getParent(worldId);
        while (current != null) {
            ancestors.add(current);
            current = getParent(current.getWorldId());
        }
        return ancestors;
    }

    public List<World> getDescendants(WorldId worldId) {
        List<World> descendants = new ArrayList<>();
        Deque<WorldId> queue = new ArrayDeque<>();
        queue.add(worldId);

        while (!queue.isEmpty()) {
            WorldId current = queue.removeFirst();
            for (World child : getChildren(current)) {
                descendants.add(child);
                queue.addLast(child.getWorldId());
            }
        }

        return descendants;
    }

    public List<World> getLeaves() {
        List<World> leaves = new ArrayList<>();
        for (World world : worlds.values()) {
            if (isLeaf(world.getWorldId())) {
                leaves.add(world);
            }
        }
        return leaves;
    }

    public int getDepth(WorldId worldId) {
        int depth = 0;
        World current = getParent(worldId);
        while (current != null) {
            depth += 1;
            current = getParent(current.getWorldId());
        }
        return depth;
    }

    public void clear() {
        worlds.clear();
        edges.clear();
        parentEdgeByChild.clear();
        childEdgeIdsByParent.clear();
        genesisId = null;
    }

    private boolean wouldCreateCycle(WorldId from, WorldId to) {
        if (from.equals(to)) {
            return true;
        }

        World parent = worlds.get(from.value());
        while (parent != null) {
            if (parent.getWorldId().equals(to)) {
                return true;
            }
            parent = getParent(parent.getWorldId());
        }
        return false;
    }
}
