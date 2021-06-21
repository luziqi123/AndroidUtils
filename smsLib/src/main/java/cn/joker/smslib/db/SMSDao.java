package cn.joker.smslib.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cn.joker.smslib.entity.SMSEntity;


@Dao
public interface SMSDao {

    /**
     * 添加一个
     * @param smsEntity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SMSEntity smsEntity);

    /**
     * 添加集合
     * @param smsEntityList
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SMSEntity> smsEntityList);

    /**
     * 全部删除
     */
    @Query("DELETE FROM sms_entity")
    void deleteAll();

    /**
     * 删除一个
     * @param smsEntity
     */
    @Delete
    void delete(SMSEntity smsEntity);

    /**
     * 根据id删除一个
     * @param id
     */
    @Query("DELETE FROM sms_entity WHERE id = :id")
    void deleteById(String id);

    /**
     * 根据id查找
     * @param id
     * @return
     */
    @Query("SELECT * FROM sms_entity WHERE id = :id")
    SMSEntity getSMSById(String id);

    /**
     * 根据status查找
     * @param status
     * @return
     */
    @Query("SELECT * FROM sms_entity WHERE status = :status")
    List<SMSEntity> getSMSByStatus(int status);

    /**
     * 根据id跟status查找
     * @param id
     * @param status
     * @return
     */
    @Query("SELECT * FROM sms_entity WHERE id = :id AND status = :status")
    SMSEntity getSMSByIdAndStatus(String id,int status);

    /**
     * 查找全部
     * @return
     */
    @Query("SELECT * FROM sms_entity")
    List<SMSEntity> getSMSAll();

}
