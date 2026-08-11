package com.quietus.util.layouts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class VerticalEvenGridLayout extends AbstractLayout{
    private final List<LayoutElement> children = new ArrayList<>();

    private int columns;
    private int rowHeight;

    public VerticalEvenGridLayout(int x, int y, int width, int columns, int rowHeight) {
        super(x, y, width, 0);
        this.columns = Math.max(1,columns);
        this.rowHeight = rowHeight;
    }

    public <T extends LayoutElement> T addChild(T child) {
        this.children.add(child);
        return child;
    }

    public void arrangeElements() {
        super.arrangeElements();

        int cellWidth = (int)Math.floorDiv(this.width, this.columns);
        int remainderWidth = this.width % this.columns;

        for (int i = 0; i < this.children.size(); i++) {
            LayoutElement child = this.children.get(i);
            int row = Math.floorDiv(i, this.columns);
            int column = i % this.columns;
            int remainderMult = (column == (this.columns-1)) ? 1 : 0;

            child.setPosition(
              this.getX() + column * cellWidth, 
              this.getY() + row * this.rowHeight
            );

            if (child instanceof AbstractWidget widget) {
                widget.setSize(
                  cellWidth + remainderMult * remainderWidth,  
                  this.rowHeight
                );
            }

            this.height = (row+1) * this.rowHeight;
        }
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> func) {
        this.children.forEach(func);
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> func) {
        super.visitWidgets(func);

        for (LayoutElement child : this.children) {
            if (child instanceof AbstractWidget widget) {
                func.accept(widget);
            }
        }
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public void setHeight(int height) {
        this.height = height;
    }

}