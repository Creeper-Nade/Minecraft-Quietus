package com.quietus.core.skill;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;

import com.quietus.core.QuietusRegistries;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class SkillComponent implements ValueIOSerializable {

    private final Map<Skill, Map<String, Number>> skillToSourceAmountMap = new HashMap<>();

    public boolean hasSkill(Skill skill) {
        return this.getTotalLevel(skill).doubleValue() > 0;
    }

    public Map<Skill, Map<String, Number>> getMap() {
        return this.skillToSourceAmountMap;
    }

    public Number getLevel(Skill skill, String source) {
        if (!this.skillToSourceAmountMap.containsKey(skill)) {
            return 0;
        }
        return this.skillToSourceAmountMap.get(skill).getOrDefault(source, 0);
    }

    public int getIntLevel(Skill skill, String source) {
        return getLevel(skill, source).intValue();
    }

    public float getFloatLevel(Skill skill, String source) {
        return getLevel(skill, source).floatValue();
    }

    public double getDoubleLevel(Skill skill, String source) {
        return getLevel(skill, source).doubleValue();
    }

    public Map<String, Number> getSourceLevels(Skill skill) {
        return this.skillToSourceAmountMap.containsKey(skill) ?
            Map.copyOf(this.skillToSourceAmountMap.get(skill))
            : Map.of();
    }

    public Number getTotalLevel(Skill skill) {
        if (!this.skillToSourceAmountMap.containsKey(skill)) {
            switch (skill.getType()) {
                case Skill.Type.INT:
                    return 0;
                case Skill.Type.DOUBLE:
                    return 0.0d;
                case Skill.Type.FLOAT:
                    return 0.0f;
                default:
                    return 0;
            }
        }
        double sum = 0.0;
        for (Number num : this.skillToSourceAmountMap.get(skill).values()) {
            sum += num.doubleValue();
        }
        return castToType(sum, skill.getType());
    }

    public void addLevel(Skill skill, Number amount, String source) {
        double current = getLevel(skill, source).doubleValue();
        double next = current + amount.doubleValue();
        setLevel(skill, next, source);
    }

    public void setLevel(Skill skill, Number value, String source) {
        double clamped = Math.clamp(value.doubleValue(), 0.0, skill.maxLevel().doubleValue());
        Number typedValue = castToType(clamped, skill.getType());
        this.skillToSourceAmountMap.computeIfAbsent(skill, k -> new HashMap<>()).put(source, typedValue);
    }

    private static Number castToType(double value, Skill.Type type) {
        return switch (type) {
            case INT -> (int) Math.round(value);
            case FLOAT -> (float) value;
            case DOUBLE -> value;
        };
    }

    @Override
    public void serialize(ValueOutput output) {
        ValueOutput.ValueOutputList list = output.childrenList("skills");

        this.skillToSourceAmountMap.forEach((skill, levelsMap) -> {
            ValueOutput skill_tag = list.addChild();
            skill_tag.putString("id", skill.id.identifier().toString());
            ValueOutput.ValueOutputList list2 = skill_tag.childrenList("levels");
            levelsMap.forEach((source, level) -> {
                ValueOutput levels_tag = list2.addChild();
                levels_tag.putString("source", source);
                switch (skill.getType()) {
                    case INT -> levels_tag.putInt("level", level.intValue());
                    case FLOAT -> levels_tag.putFloat("level", level.floatValue());
                    case DOUBLE -> levels_tag.putDouble("level", level.doubleValue());
                }
            });
        });
    }

    @Override
    public void deserialize(ValueInput input) {
        this.skillToSourceAmountMap.clear();

        ValueInput.ValueInputList list = input.childrenListOrEmpty("skills");
        for (ValueInput tag : list) {
            Skill skill = QuietusRegistries.SKILL_REGISTRY.getValue(Identifier.parse(tag.getStringOr("id", "quietus:none")));
            if (skill == null) continue;

            Map<String, Number> levelsMap = new HashMap<>();
            ValueInput.ValueInputList list2 = tag.childrenListOrEmpty("levels");
            for (ValueInput tag2 : list2) {
                String source = tag2.getStringOr("source", "none");
                Number level = switch (skill.getType()) {
                    case INT -> tag2.getIntOr("level", 0);
                    case FLOAT -> tag2.getFloatOr("level", 0.0f);
                    case DOUBLE -> tag2.getDoubleOr("level", 0.0);
                };
                levelsMap.put(source, level);
            }
            this.skillToSourceAmountMap.put(skill, levelsMap);
        }
    }

}
