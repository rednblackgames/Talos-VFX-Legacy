package games.rednblack.talos.runtime;

import com.badlogic.gdx.utils.Pool;

public class ParticleEffectInstancePool extends Pool<ParticleEffectInstancePool.PooledParticleEffectInstance> {

    private final ParticleEffectDescriptor particleEffectDescriptor;

    public ParticleEffectInstancePool(ParticleEffectDescriptor descriptor) {
        particleEffectDescriptor = descriptor;
    }

    public ParticleEffectInstancePool(ParticleEffectDescriptor descriptor, int initialCapacity) {
        super(initialCapacity);
        particleEffectDescriptor = descriptor;
    }

    public ParticleEffectInstancePool(ParticleEffectDescriptor descriptor, int initialCapacity, int max) {
        super(initialCapacity, max);
        particleEffectDescriptor = descriptor;
    }

    @Override
    protected PooledParticleEffectInstance newObject() {
        PooledParticleEffectInstance particleEffectInstance = new PooledParticleEffectInstance(particleEffectDescriptor);
        particleEffectDescriptor.setEffectReference(particleEffectInstance);

        for(ParticleEmitterDescriptor emitterDescriptor: particleEffectDescriptor.emitterModuleGraphs) {
            particleEffectInstance.addEmitter(emitterDescriptor);
        }

        particleEffectInstance.sortEmitters();

        // create default scope
        particleEffectInstance.setScope(new ScopePayload());

        return particleEffectInstance;
    }

    public ParticleEffectDescriptor getParticleEffectDescriptor() {
        return particleEffectDescriptor;
    }

    @Override
    public void free(PooledParticleEffectInstance object) {
        super.free(object);

        object.restart();
    }

    public class PooledParticleEffectInstance extends ParticleEffectInstance {
        public PooledParticleEffectInstance(ParticleEffectDescriptor particleEffectDescriptor) {
            super(particleEffectDescriptor);
        }

        public void free () {
            ParticleEffectInstancePool.this.free(this);
        }
    }
}
