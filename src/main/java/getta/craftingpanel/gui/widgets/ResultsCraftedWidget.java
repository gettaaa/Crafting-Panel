package getta.craftingpanel.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.GuiUtils;
import getta.craftingpanel.CraftingPanel;
import getta.craftingpanel.Utils;
import getta.craftingpanel.CraftingPanelItemOutput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResultsCraftedWidget extends WidgetBase {

    @NotNull List<CraftingPanelItemOutput> results;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private boolean showResults;
    private List<CraftingPanelItemOutput> currentShow = null;

    public ResultsCraftedWidget(int x, int y, int width, int height, List<CraftingPanelItemOutput> items) {
        super(x, y, width, height);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.results = new ArrayList<>();
        this.showResults = false;

        if (items != null && !items.isEmpty()) {
            this.results = items;
            this.showResults = true;
        }
    }

    public void cycle() {
        if (Utils.cycle == Utils.outputTypes.length - 1) {
            Utils.cycle = 0;
        } else {
            Utils.cycle++;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, MatrixStack matrixStack) {

        RenderUtils.color(1f, 1f, 1f, 1f);
        matrixStack.translate(0, 0, 1);

        RenderUtils.drawOutlinedBox(this.x, this.y, this.width, this.height + 4, 0xA0000000, GuiBase.COLOR_HORIZONTAL_BAR);

        this.textRenderer.drawWithShadow(matrixStack, Text.of("Results: " + Utils.outputTypes[Utils.cycle]), this.x + 4, this.y + 5, Color.WHITE.getRGB());

        String toggle = CraftingPanel.hud ? "ON" : "OFF";
        int color = CraftingPanel.hud ? Color.GREEN.getRGB() : Color.RED.getRGB();
        this.textRenderer.draw(matrixStack, toggle, 50 + this.textRenderer.getWidth("Calculate Materials  Export") + 3, MinecraftClient.getInstance().getWindow().getScaledHeight() - 23, color);

        if (showResults && !this.results.isEmpty()) {

            int xQuantity = 0;
            int yQuantity = 0;

            List<CraftingPanelItemOutput> items;
            if (results.get(0).getMaterials() != null) {
                items = Utils.convertItemToMaterials(results);
            } else {
                items = results;
            }
            float biggest = 0;

            this.currentShow = items;

            for (CraftingPanelItemOutput item : items) {

                float realCount = item.getCount();

                if (Utils.cycle == 2) {
                    item.setCount(item.getCount() / Utils.shulker);
                } else if (Utils.cycle == 1) {
                    item.setCount(item.getCount() / Utils.stack);
                }

                if (item.getCount() > biggest) {

                    biggest = item.getCount();
                }

                ItemStack itemStack = Utils.getItemStackFromItemCommandOutputName(item.getName());

                try {
                    String amount = String.valueOf((int) item.getCount());

                    this.mc.getItemRenderer().renderInGui(itemStack, this.x + 7 + xQuantity, this.y + 15 + yQuantity);
                    this.textRenderer.drawWithShadow(matrixStack, "x" + amount, this.x + 30 + xQuantity, this.y + 20 + yQuantity, Color.WHITE.getRGB());

                    if (this.x + 7 + xQuantity <= mouseX && this.x + 24 + xQuantity >= mouseX &&
                            this.y + 15 + yQuantity <= mouseY && this.y + 30 + yQuantity >= mouseY) {

                        List<Text> text = new ArrayList<>();
                        int stacks = 0;
                        int shulkers = 0;
                        text.add(Text.of("Total Items: §l" + (int) realCount));
                        while (realCount >= Utils.shulker) {
                            shulkers++;
                            realCount -= Utils.shulker;
                        }
                        while (realCount >= Utils.stack) {
                            stacks++;
                            realCount -= Utils.stack;
                        }
                        String quantity = "Shulkers: §l" + shulkers + " §rStacks: §l" + stacks + " §rItems: §l" + (int) realCount;
                        text.add(0, Text.of("§nItems:"));
                        text.add(0, Text.of(quantity));
                        text.add(0, Text.of("§nSimple:"));
                        text.add(0, Text.of("§l" + Utils.getItemStackFromItemCommandOutputName(item.getName()).getName().getString()));
                        GuiUtils.getCurrentScreen().renderTooltip(matrixStack, text, Optional.empty(), this.x - 24 + xQuantity, this.y - 25 + yQuantity);
                    }

                    yQuantity += 20;

                    if (yQuantity + 20 >= this.height) {

                        if (xQuantity + this.textRenderer.getWidth("x " + (int) biggest) + 40 >= this.width) {
                            break;
                        }

                        yQuantity = 0;
                        xQuantity += 22 + this.textRenderer.getWidth("x " + (int) biggest);
                    }


                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    public void clearResults() {

        this.results = new ArrayList<>();
        this.showResults = false;
    }

    public void receiveResults(@NotNull List<CraftingPanelItemOutput> results) {

        this.results = results;
        this.showResults = true;
    }

    public void setHud() {
        CraftingPanel.hud = !CraftingPanel.hud;
        if (CraftingPanel.hud && this.currentShow != null) {
            CraftingPanel.items = this.currentShow;
        }
    }
}
