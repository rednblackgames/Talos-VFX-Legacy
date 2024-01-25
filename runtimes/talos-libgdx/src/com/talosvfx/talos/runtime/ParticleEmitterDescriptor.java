/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.runtime;

import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.runtime.modules.*;
import com.talosvfx.talos.runtime.modules.AbstractModule;

public class ParticleEmitterDescriptor {

    private final ParticleEffectDescriptor particleEffectResourceDescriptor;

    Array<AbstractModule> modules = new Array<>();

    ParticleModule particleModule;
    EmitterModule emitterModule;

    private int sortPosition;

    public static ObjectSet<Class> registeredModules;

    public ParticleEmitterDescriptor (ParticleEffectDescriptor descriptor) {
        this.particleEffectResourceDescriptor = descriptor;
        registerModules();
    }

    public static ObjectSet<Class> getRegisteredModules() {
        registerModules();
        return registeredModules;
    }

    public static void registerModules() {
        if(registeredModules == null) {
            registeredModules = new ObjectSet<>();
            registeredModules.add(EmitterModule.class);
            registeredModules.add(InterpolationModule.class);
            registeredModules.add(InputModule.class);
            registeredModules.add(ParticleModule.class);
            registeredModules.add(StaticValueModule.class);
            registeredModules.add(RandomRangeModule.class);
            registeredModules.add(MixModule.class);
            registeredModules.add(MathModule.class);
            registeredModules.add(CurveModule.class);
            registeredModules.add(Vector2Module.class);
            registeredModules.add(ColorModule.class);
            registeredModules.add(DynamicRangeModule.class);
            registeredModules.add(GradientColorModule.class);
            registeredModules.add(TextureModule.class);
            registeredModules.add(EmConfigModule.class);
            registeredModules.add(OffsetModule.class);
            registeredModules.add(RandomInputModule.class);
            registeredModules.add(NoiseModule.class);
            registeredModules.add(PolylineModule.class);
            registeredModules.add(RibbonModule.class);
            registeredModules.add(FromToModule.class);
            registeredModules.add(GlobalScopeModule.class);
            registeredModules.add(FlipbookModule.class);
            registeredModules.add(ShadedSpriteModule.class);
            registeredModules.add(FakeMotionBlurModule.class);
            registeredModules.add(VectorFieldModule.class);
            registeredModules.add(RadToCartModule.class);
            registeredModules.add(CartToRadModule.class);
            registeredModules.add(AttractorModule.class);
            registeredModules.add(ForceApplierModule.class);
            registeredModules.add(NinePatchModule.class);
        }
    }

    public boolean addModule (AbstractModule module) {
        boolean added = true;
        if (module instanceof ParticleModule) {
            if (particleModule == null) {
                particleModule = (ParticleModule)module;
            } else {
                added = false;
            }
        }
        if (module instanceof EmitterModule) {
            if (emitterModule == null) {
                emitterModule = (EmitterModule)module;
            } else {
                added = false;
            }
        }

        if (added) {
            modules.add(module);
        }

        return added;

    }

    public void removeModule(AbstractModule module) {
        // was this module connected to someone?
        for(AbstractModule toModule: modules) {
            if(toModule.isConnectedTo(module)) {
                toModule.detach(module);
            }
        }

        modules.removeValue(module, true);

        if(module instanceof ParticleModule) {
            particleModule = null;
        }
        if(module instanceof EmitterModule) {
            emitterModule = null;
        }
    }

    public void connectNode(AbstractModule from, AbstractModule to, int slotFrom, int slotTo) {
        // slotTo is the input of module to
        // slotFrom is the output of slot from
        from.attachModuleToMyOutput(to, slotFrom, slotTo);
        to.attachModuleToMyInput(from, slotTo, slotFrom);
    }

    public void removeNode(AbstractModule module, int slot,boolean isInput) {
        module.detach(slot, isInput);
    }

    public ParticleModule getParticleModule() {
        return particleModule;
    }

    public void resetRequesters() {
        for(AbstractModule module: modules) {
            module.resetLastRequester();
        }
    }

    public EmitterModule getEmitterModule() {
        return emitterModule;
    }

    public Array<AbstractModule> getModules() {
        return modules;
    }

    public ParticleEffectDescriptor getEffectDescriptor() {
        return particleEffectResourceDescriptor;
    }

    public boolean isContinuous() {
        return getEmitterModule().isContinuous();
    }

    public int getSortPosition() {
        return sortPosition;
    }

    public void setSortPosition(int sortPosition) {
        this.sortPosition = sortPosition;
    }

}
