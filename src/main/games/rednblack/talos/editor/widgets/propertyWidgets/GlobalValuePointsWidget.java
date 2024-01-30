package games.rednblack.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import games.rednblack.talos.TalosMain;
import games.rednblack.talos.runtime.bvb.AttachmentPoint;

import java.util.function.Supplier;

public class GlobalValuePointsWidget extends PropertyWidget<Array<AttachmentPoint>> {

    GlobalValueListContainer listContainer;

    public Supplier<Array<Bone>> boneListSuppler;

    public GlobalValuePointsWidget() {
        super();
    }

    @Override
    public PropertyWidget clone() {
        GlobalValuePointsWidget clone = (GlobalValuePointsWidget) super.clone();
        clone.boneListSuppler = this.boneListSuppler;

        return clone;
    }


    public GlobalValuePointsWidget(Supplier<Array<AttachmentPoint>> supplier, Supplier<Array<Bone>> boneListSuppler) {
        super(null, supplier, null, false);
        this.boneListSuppler = boneListSuppler;
        build(null);
    }

    @Override
    public Actor getSubWidget() {
        listContainer = new GlobalValueListContainer(TalosMain.Instance().getSkin());
        listContainer.setBoneList(boneListSuppler.get());
        return listContainer;
    }

    @Override
    public void updateWidget(Array<AttachmentPoint> value) {
        listContainer.setData(value);
    }
}
