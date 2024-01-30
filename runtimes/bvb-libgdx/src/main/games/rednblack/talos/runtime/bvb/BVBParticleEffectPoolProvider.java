package games.rednblack.talos.runtime.bvb;

import games.rednblack.talos.runtime.ParticleEffectInstancePool;

public interface BVBParticleEffectPoolProvider {
    ParticleEffectInstancePool getPool(String effectName);
}
