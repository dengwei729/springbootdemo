package com.example.demo.start.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BaseStatsManager {

    protected Map<String, List<Stat>> statMap;

    public BaseStatsManager() {
        this.statMap = new HashMap<String, List<Stat>>();
    }

    protected void start(String beanName) {
        Stat stat = new Stat(System.currentTimeMillis());

        if (this.statMap.containsKey(beanName)) {
            List<Stat> list = this.statMap.get(beanName);
            assert list.get(list.size()-1).endTimestamp >0L;
            list.add(stat);
        } else {
            List<Stat> list = new ArrayList<>();
            list.add(stat);
            this.statMap.put(beanName, list);
        }
    }

    protected void end(String beanName) {
        assert this.statMap.containsKey(beanName);

        List<Stat> list = this.statMap.get(beanName);
        Stat stat = list.get(list.size() - 1);

        assert stat.endTimestamp == 0L;
        stat.endTimestamp = System.currentTimeMillis();
    }

    protected long getTotal() {
        int total = 0;

        for(List<Stat> list : statMap.values()) {
            total += accumulate(list);
        }
        return total;
    }

    protected List<Map.Entry<String, List<Stat>>> getTopN(int n) {
        List<Map.Entry<String, List<Stat>>> result = new ArrayList<Map.Entry<String, List<Stat>>>(statMap.entrySet());

        Collections.sort(result, new Comparator<Entry<String, List<Stat>>>() {
            @Override
            public int compare(Map.Entry<String, List<Stat>> o1, Map.Entry<String, List<Stat>> o2) {

                return (int) (accumulate(o2.getValue()) - accumulate(o1.getValue()));
            }
        });

        return result.subList(0, result.size() > n ? n : result.size());
    }

    public Long get(String beanName) {
        List<Stat> list = statMap.get(beanName);
        if (list == null) {
            return null;
        } else {
            return accumulate(list);
        }
    }

    static Long accumulate(List<Stat> list) {
        assert list != null;
        Long result = 0L;
        for(Stat stat : list) {
            result += stat.getTimeCost();
        }

        return result;
    }
}
