package com.example.bean;/**
 * 功能描述
 *
 * @author wj
 * @date 2023/08/08  10:22
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *@ClassName Admin
 *@Description TODO
 *@Author wj
 *@Date 2023/8/8 10:22
 *@Version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("admin")
public class Admin {
    @TableId(value = "Admin_ID", type = IdType.AUTO)
    private Integer AdminID;
    @TableField("Admin_Name")
    private byte[] AdminName;
    @TableField("Admin_Password")
    private byte[] AdminPassword;
    @TableField("FaceData_Admin")
    private byte[] FaceDataAdmin;
    @TableField("Admin_Image_1")
    private byte[] AdminImage1;
    @TableField("Admin_Image_2")
    private byte[] AdminImage2;
    @TableField("Admin_Image_3")
    private byte[] AdminImage3;
    @TableField("Admin_Image_4")
    private byte[] AdminImage4;
    @TableField("Admin_Image_5")
    private byte[] AdminImage5;
    @TableField("Admin_Image_6")
    private byte[] AdminImage6;
    @TableField("Admin_Image_7")
    private byte[] AdminImage7;
}
