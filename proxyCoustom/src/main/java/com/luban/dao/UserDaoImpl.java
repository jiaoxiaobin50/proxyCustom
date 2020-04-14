package com.luban.dao;

public class UserDaoImpl implements UserDao{

    public void query(){
        System.out.println("假装查询数据库");
    }

    public void query(String aa){
        System.out.println(aa);
    }
}
