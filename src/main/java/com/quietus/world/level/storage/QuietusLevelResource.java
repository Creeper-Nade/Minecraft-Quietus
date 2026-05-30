package com.quietus.world.level.storage;

public enum QuietusLevelResource {

    SKILL_TREE_PROGRESS("skill_tree");

    private final String id;
    QuietusLevelResource(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

}
