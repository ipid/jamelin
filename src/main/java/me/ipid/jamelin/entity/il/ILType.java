package me.ipid.jamelin.entity.il;

import me.ipid.jamelin.entity.*;

import java.util.List;

public interface ILType {

    /**
     * 获取该类型需要占用多少个槽。
     *
     * @return 槽数
     */
    int getSize();

    /**
     * 获取当前类型的内存布局。
     *
     * @param container 用于填入槽信息的容器
     */
    void fillMemoryLayout(List<MemorySlot> container);
}
