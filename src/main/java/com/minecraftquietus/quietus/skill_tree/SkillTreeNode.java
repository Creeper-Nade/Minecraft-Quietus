package com.minecraftquietus.quietus.skill_tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.minecraftquietus.quietus.core.skill.Skill;
import com.minecraftquietus.quietus.util.SkillUtil;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class SkillTreeNode {
    
    private final ResourceLocation id;

    /* protected final Set<SkillTreeNode> prerequisiteEvery = new ReferenceOpenHashSet<>();
    protected final Set<Set<SkillTreeNode>> prerequisiteOneFromEach = new ReferenceOpenHashSet<>(); */
    protected final Set<SkillTreeNode> children = new ReferenceOpenHashSet<>();


    public SkillTreeNode(ResourceLocation id, Set<SkillTreeNode> prerequisiteEvery, Set<Set<SkillTreeNode>> prerequisiteOneFromEach) {
        this.id = id;
        /* this.prerequisiteEvery.addAll(prerequisiteEvery);
        this.prerequisiteOneFromEach.addAll(prerequisiteOneFromEach); */
    }

    private final List<Action> actions = new ArrayList<>();

    public void addAction(Skill skill, int amount) {
        this.actions.add(new Action(skill, amount, this.id.toString()));
    }

    public void apply(Player player) {
        for (Action action : this.actions) {
            action.apply(player);
        }
    }

    record Action(
        Skill skill,
        int amount,
        String source
    ) {
        public void apply(Player player) {
            SkillUtil.addSkillLevel(player, this.skill, this.amount, this.source);
        }
    }

    public ResourceLocation getId() {
        return this.id;
    }
}
