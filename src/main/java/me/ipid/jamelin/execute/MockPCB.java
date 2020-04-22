package me.ipid.jamelin.execute;

import me.ipid.jamelin.entity.state.StateNode;
import me.ipid.jamelin.exception.RuntimeExceptions.JamelinRuntimeException;

import java.util.ArrayList;

public class MockPCB extends ProcessControlBlock {

    public MockPCB() {
        super(0, new ArrayList<>(), new StateNode());
    }

    @Override
    public StateNode getCurrState() {
        throw new Error("试图在全局初始化语句中获取初始状态");
    }

    @Override
    public ProcessControlBlock setCurrState(StateNode currState) {
        throw new Error("试图在全局初始化语句中设置初始状态");
    }

    @Override
    public int getPid() {
        throw new JamelinRuntimeException("试图在全局变量中获取 _pid");
    }

    @Override
    public ProcessControlBlock setPid(int pid) {
        throw new Error("试图在全局变量中设置 pid");
    }

    @Override
    public int getProcessMemory(int offset) {
        throw new Error("试图在全局变量中获取进程内存");
    }

    @Override
    public void setProcessMemory(int offset, int value) {
        throw new Error("试图在全局变量中设置进程内存");
    }
}
