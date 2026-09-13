package skye.emcbaubles.queue;

import net.minecraft.entity.player.EntityPlayer;

import java.lang.ref.WeakReference;

public abstract class ExpirablePlayer {
    protected final WeakReference<EntityPlayer> PLAYER;

    ExpirablePlayer(EntityPlayer player) {
        this.PLAYER = new WeakReference<>(player);
    }

    protected EntityPlayer getPlayer() { return PLAYER.get(); }
    protected boolean playerReferenceInvalid() { return (PLAYER.get() == null || getPlayer().isDead); }
}
