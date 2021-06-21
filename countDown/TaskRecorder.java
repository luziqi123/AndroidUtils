package com.longface.common.countDown;

import android.os.SystemClock;

import com.google.gson.Gson;
import com.longface.common.string.EditUtil;

import java.util.HashMap;

class TaskRecorder {

    static HashMap<String, TaskModel> taskRecordMap = new HashMap<>();


    /**
     * 倒计时任务是否存在
     *
     * @return
     */
    public TaskModel hasCountDownTask(String tag) {
        if (taskRecordMap.containsKey(tag)) {
            return taskRecordMap.get(tag);
        } else {
            // 如果文件中有
            String recordJson = EditUtil.getString(tag, null);
            if (recordJson != null) {
                // 从文件存到内存
                TaskModel taskModel = new Gson().fromJson(recordJson, TaskModel.class);
                // 将倒计时任务同步
                TaskModel synTaskModel = synchronizationTask(taskModel);
                if (synTaskModel != null) {
                    // 如果没有过期
                    return synTaskModel;
                }
            }
        }
        // 如果没有或者已经过期 , 就直接删除这个任务
        delTaskRecord(tag);
        return null;
    }

    /**
     * 将任务加载到内存
     *
     * @param tag
     * @return
     */
    public TaskModel loadToMemory(String tag) {
        if (taskRecordMap.containsKey(tag)) {
            // 他如果内存中有 , 直接返回
            return taskRecordMap.get(tag);
        } else {
            // 如果文件中有
            String recordJson = EditUtil.getString(tag, null);
            if (recordJson != null) {
                // 从文件存到内存
                TaskModel taskModel = new Gson().fromJson(recordJson, TaskModel.class);
                // 将倒计时任务同步
                TaskModel synTaskModel = synchronizationTask(taskModel);
                if (synTaskModel != null) {
                    // 如果没有过期
                    updateTaskRecord(tag, synTaskModel);
                    return synTaskModel;
                }
            }
        }
        // 如果没有或者已经过期 , 就直接删除这个任务
        delTaskRecord(tag);
        return null;
    }

    /**
     * 创建任务记录
     *
     * @param tag
     * @param countDownSec
     */
    public boolean createTaskRecord(String tag, int countDownSec) {
        TaskModel countDownTask = loadToMemory(tag);
        if (countDownTask != null) {
            return false;
        }
        TaskModel taskMode = new TaskModel();
        taskMode.setPause(false);
        taskMode.setStartTimeMillis(SystemClock.elapsedRealtime());
        taskMode.setCountDownSec(countDownSec);
        EditUtil.save(tag, new Gson().toJson(taskMode));
        taskRecordMap.put(tag, taskMode);
        return true;
    }

    /**
     * 删除记录
     *
     * @param tag
     */
    public void delTaskRecord(String tag) {
        taskRecordMap.remove(tag);
        EditUtil.mmkv.putString(tag, null);
    }

    /**
     * 同步倒计时
     *
     * @param taskMode
     * @return
     */
    private TaskModel synchronizationTask(TaskModel taskMode) {
        if (taskMode.isPause()) {
            return taskMode;
        }
        long l = (SystemClock.elapsedRealtime() - taskMode.getStartTimeMillis()) / 1000;
        if (l >= taskMode.getCountDownSec()) {
            return null;
        } else {
            taskMode.setCountDownSec((int) (taskMode.getCountDownSec() - l));
            taskMode.setStartTimeMillis(SystemClock.elapsedRealtime());
            return taskMode;
        }
    }

    /**
     * 修改任务的暂停开始状态
     *
     * @param tag
     * @param taskMode
     */
    void updateTaskRecord(String tag, TaskModel taskMode) {
        taskRecordMap.put(tag, taskMode);
        EditUtil.save(tag, new Gson().toJson(taskMode));
    }

}
