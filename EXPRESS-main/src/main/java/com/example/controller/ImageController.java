package com.example.controller;/**
 * 功能描述
 *
 * @author wj
 * @date 2023/08/08  10:22
 */

import com.example.bean.Admin;
import com.example.bean.Order;
import com.example.mapper.OrderMapper;
import com.example.service.AdminService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 *@ClassName ImageController
 *@Description TODO
 *@Author wj
 *@Date 2023/8/8 10:22
 *@Version 1.0
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/image",produces = "image/jpg")
public class ImageController {

    @Resource
    AdminService adminService;

    @Resource
    OrderMapper orderMapper;

    @ResponseBody
    @GetMapping(value = "/face/{id}")
    public ResponseEntity<byte[]> FaceImage(@PathVariable("id")Integer id) throws IOException, SQLException {
        Order order = orderMapper.selectById(id);
        byte[] imageData = order.getAuthimage();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    @ResponseBody
    @GetMapping(value = "/flag/{id}")
    public ResponseEntity<byte[]> FlagImage(@PathVariable("id")Integer id) throws IOException, SQLException {
        Order order = orderMapper.selectById(id);
        byte[] imageData = order.getQrimage();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

}

