package com.example.service.impl;/**
 * 功能描述
 *
 * @author wj
 * @date 2023/08/08  10:30
 */

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bean.Admin;
import com.example.bean.CarStation;
import com.example.mapper.AdminMapper;
import com.example.mapper.CarStationMapper;
import com.example.service.AdminService;
import com.example.service.CarStationService;
import org.springframework.stereotype.Service;

/**
 *@ClassName AdminServiceImpl
 *@Description TODO
 *@Author wj
 *@Date 2023/8/8 10:30
 *@Version 1.0
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
        implements AdminService {
}
