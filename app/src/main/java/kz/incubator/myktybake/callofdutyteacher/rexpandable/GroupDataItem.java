package kz.incubator.myktybake.callofdutyteacher.rexpandable;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupDataItem implements Serializable {
    private String parentName;
    private ArrayList<StudentsItem> childDataItems;

    public GroupDataItem(ArrayList<StudentsItem> childDataItems) {
        this.childDataItems = childDataItems;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public ArrayList<StudentsItem> getChildDataItems() {
        return childDataItems;
    }

    public void setChildDataItems(ArrayList<StudentsItem> childDataItems) {
        this.childDataItems = childDataItems;
    }
}