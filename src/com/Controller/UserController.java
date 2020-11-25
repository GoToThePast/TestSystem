package com.Controller;

import com.Entity.User;
import com.Service.UserService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@WebServlet(name = "UserController",urlPatterns = "/userServlet")
public class UserController extends HttpServlet {
    private UserService userService=new UserService();
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        String contentType = request.getContentType();
        System.out.println("contentType="+contentType);
        if ("null".equals(contentType))
        {
            register(request,response);
            return;
        }

        String method = request.getParameter("method");
        if(method.equals("login"))
            login(request,response);
        if(method.equals("register"))
            register(request,response);
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    private void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = null;
        //获取上传的图片
        String photoPath = request.getServletContext().getRealPath("/photo/");
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        try{
            List<FileItem> items = upload.parseRequest(request);
            if (!items.isEmpty()){
                 user=new User();
            }
            Iterator<FileItem> iterator = items.iterator();
            while (iterator.hasNext()){
                FileItem item = iterator.next();
                if (item.isFormField()){
                    if (item.getFieldName().equals("username")) {
                        user.setUsername(new String(item.getString().getBytes("iso-8859-1"), "utf-8"));
                    }
                    else if (item.getFieldName().equals("password")) {
                        user.setPassword(new String(item.getString().getBytes("iso-8859-1"), "utf-8"));
                    }
                    else if (item.getFieldName().equals("name")) {
                        user.setName(new String(item.getString().getBytes("iso-8859-1"), "utf-8"));
                    }
                    else if (item.getFieldName().equals("age")) {
                        user.setAge(Integer.parseInt(item.getString()));
                    }
                    else if (item.getFieldName().equals("sex")) {
                        user.setSex(new String(item.getString().getBytes("iso-8859-1"), "utf-8"));
                    }
                    else if (item.getFieldName().equals("role")) {
                        user.setRole(new String(item.getString().getBytes("iso-8859-1"), "utf-8"));
                    }
                    else if (item.getFieldName().equals("telephone")) {
                        user.setTelephone(new String(item.getString().getBytes("iso-8859-1"), "utf-8"));
                    }
                }else{
                    //加上系统毫秒数，防止文件重名
                    String name = System.currentTimeMillis()+item.getName();
                    System.out.println(photoPath + name);
                    File file = new File(photoPath + name);
                    item.write(file);
                    //写入数据库
                    user.setPicUrl("photo/"+name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传文件出错");
            return;
        }

        //调用服务
        userService.insert(user);
        //跳转界面
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    public void login(HttpServletRequest request,HttpServletResponse response)throws ServletException, IOException{
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        //用户查询
        User user=userService.queryUser(username,password);
        if(user!=null){
            System.out.println("恭喜你，" + user.getName() + "登录成功，即将跳转...");
            //添加session
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            request.setAttribute("username",user.getName());
            request.getRequestDispatcher("WEB-INF/user/studentIndex.jsp").forward(request, response);
        }
        else {
            System.out.println("登陆失败，请检查用户名或密码是否正确");
            request.setAttribute("login_msg", "登陆失败，请检查用户名或密码是否正确");
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }

}
