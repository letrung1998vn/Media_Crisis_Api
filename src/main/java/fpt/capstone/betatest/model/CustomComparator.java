package fpt.capstone.betatest.model;

import java.util.Comparator;

public class CustomComparator implements Comparator<CrisisModel> {
    @Override
    public int compare(CrisisModel o1, CrisisModel o2) {
        return o2.getDetectDate().compareTo(o1.getDetectDate());
    }
}
