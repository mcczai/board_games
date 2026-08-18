package net.mcczai.cardduel.init;

import net.mcczai.cardduel.duel.DuelSeat;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static net.mcczai.cardduel.CardduelMod.MODID;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

    /**
     * 玩家对局座位。不持久化：掉线即离座（对局中掉线判负，见 P1-1b）。
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DuelSeat>> DUEL_SEAT =
            ATTACHMENT_TYPES.register("duel_seat",
                    () -> AttachmentType.<DuelSeat>builder(() -> null).build());
}
