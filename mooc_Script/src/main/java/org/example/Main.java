package org.example;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.Keys;
import java.lang.reflect.Type;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;


//目前问题,没有成功终结线程



public class Main {
    static int sleepTime = 3000;


    static int wait_to_interrupt = 0;

    //C:\Users\19352\AppData\Local\Google\Chrome\User Data
    //第一个参数输入网址，第二个参数放chromedriver位置第三个参数放谷歌用户信息位置
    public static void main(String[] args) throws InterruptedException {
        my_Url web_url = new my_Url(args[0]);

        // 设置浏览器驱动路径
        System.setProperty("webdriver.chrome.driver", args[1]);
        ChromeOptions user_option = new ChromeOptions();
        user_option.addArguments(String.format("--user-data-dir=%s",args[2]));//-----------------
        user_option.addArguments("--disable-infobars");
        // 创建Chrome浏览器实例
        WebDriver driver = new ChromeDriver(user_option);
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        //打开网页
        driver.get(web_url.getHead_link() + "/announce");
        //获取cookie
        Set<Cookie> cookies = driver.manage().getCookies();


        WebDriverWait wait_1 = new WebDriverWait(driver,60);
        wait_1.until(presenceOfElementLocated(By.cssSelector(".f-thide.f-fc3")));
        List<WebElement> link = driver.findElements(By.cssSelector(".f-thide.f-fc3"));
        link.get(2).click();


        WebDriverWait wait_2= new WebDriverWait(driver,60);
        wait_1.until(presenceOfElementLocated(By.cssSelector(".m-learnChapterNormal.f-pr")));
        //展开列表
        List<WebElement> elements = driver.findElements(By.cssSelector(".m-learnChapterNormal.f-pr"));
        for (WebElement temp:elements){
            List<WebElement> judge = temp.findElements(By.cssSelector(".u-learnLesson.normal.f-cb.f-pr"));
            if(judge.isEmpty()){
                temp.click();
            }
            TimeUnit.MILLISECONDS.sleep(300);
        }
        //获取所有没完成的，不包括下面
        List<WebElement> tags_1 = driver.findElements(By.cssSelector(".f-icon.lsicon.learned"));
        List<WebElement> tags = driver.findElements(By.cssSelector(".f-icon.lsicon"));
        tags.removeAll(tags_1);

        //获取所有小表头，用于id的确定
        List<WebElement> all_ele =  driver.findElements(By.cssSelector(".u-learnLesson.f-cb.f-pr"));
        List<String> all_auto_id = new ArrayList<String>();
        for (WebElement temp : all_ele){
            all_auto_id.add(temp.getAttribute("id"));
        }


        //模拟完成课程
        int now = 0;
        int least_thread = Thread.activeCount();
        Queue<Thread> thread_q = new LinkedList<>();
        for (WebElement temp : tags){
            int total = Thread.activeCount();
            //防止进程过多造成卡顿
            while(total-least_thread >= 10){
                TimeUnit.SECONDS.sleep(10);
                total = Thread.activeCount();
            }
            //修改id
            //目前元素的父父级auto_id
            String auto_id = temp.findElement(By.xpath("../../.")).getAttribute("id");////

            while(true){
                if(!Objects.equals(auto_id, all_auto_id.get(now)))
                {
                    web_url.add_Id();
                    now++;
                }
                else
                    break;
            }

            //获取tag，用于下面if
            WebElement temp_ele = temp.findElement(By.cssSelector(".tag"));

            //要进入的链接
            String url = web_url.getLesson_link(temp.getAttribute("data-cid"));
            if(Objects.equals(temp_ele.getText(), "富文本")){
                Finish_0 other_win = new Finish_0(url,cookies);
//                executorService.submit(other_win);
                other_win.start();
//                thread_q.offer(other_win);
//                System.out.println("目前进程数：" + Thread.activeCount());
            }
            else if(Objects.equals(temp_ele.getText(), "文档")){
                Finish_1 other_win = new Finish_1(url,cookies);
                other_win.start();
            }
            else if(Objects.equals(temp_ele.getText(), "视频")){
                Finish_2 other_win = new Finish_2(url,cookies);
                other_win.start();
            }
            else if(Objects.equals(temp_ele.getText(), "讨论")){
                Finish_3 other_win = new Finish_3(url,cookies);
                other_win.start();
            }
//            else if(Objects.equals(temp_ele.getText(), "测验")){
//                Finish_4 other_win = new Finish_4(url,cookies);
//                other_win.start();
//            }
            else{
                continue;
//                System.out.println(temp_ele.getText() + 1);
//                System.out.println(Objects.equals(temp_ele.getText(), "视频"));
            }
        }

        System.out.println("结束");
        driver.quit();
    }
    static class my_Url{
        private String head_link;
        private String main_link;
        private String id_String;
        private long id_num;
        public my_Url(String link){
            this.head_link = link.split("/content\\?type=detail&id=|&")[0];
            this.id_String = link.split("/content\\?type=detail&id=|&")[1];
            this.id_num = Long.parseLong(id_String);
            this.main_link = head_link + "/content";

        }

        public String getLesson_link(String cid){
            return main_link + "?type=detail&id=" + id_String + "&cid=" + cid;
        }

        public String getHead_link(){
            return head_link;
        }

        public String getMain_link(){
            return main_link;
        }

        public void add_Id(){
            id_num++;
            id_String = Long.toString(id_num);
        }
    }

    //0富文本，1文档，2视频，3讨论，4测验
    //测验还没做
    static class Finish_0 extends Thread{
        String web_url;
        Set<Cookie> cookies;

        public Finish_0(String web_url,Set<Cookie> cookies){
            this.web_url = web_url;
            this.cookies = cookies;
        }
        @Override
        public void run(){

            WebDriver driver = new ChromeDriver();

            driver.get("https://www.icourse163.org");
            for (Cookie temp : cookies){
                driver.manage().addCookie(temp);
            }
            driver.get(web_url);
            driver.navigate().refresh();
            try {
                Thread.sleep(sleepTime);
            }catch (InterruptedException ex){
                System.out.println(1);
            }
            driver.quit();
        }
    }
    static class Finish_1 extends Thread{
        String web_url;
        Set<Cookie> cookies;

        public Finish_1(String web_url,Set<Cookie> cookies){
            this.web_url = web_url;
            this.cookies = cookies;
        }
        @Override
        public void run(){

            WebDriver driver = new ChromeDriver();
            driver.get("https://www.icourse163.org");
            for (Cookie temp : cookies){
                driver.manage().addCookie(temp);
            }
            driver.get(web_url);
            driver.navigate().refresh();
            try {
                Thread.sleep(sleepTime);
            }catch (InterruptedException ex){
                System.out.println(1);
            }
            driver.quit();
        }
    }
    static class Finish_2 extends Thread{
        String web_url;
        Set<Cookie> cookies;

        public Finish_2(String web_url,Set<Cookie> cookies){
            this.web_url = web_url;
            this.cookies = cookies;
        }
        @Override
        public void run(){

            WebDriver driver = new ChromeDriver();
            driver.get("https://www.icourse163.org");
            for (Cookie temp : cookies){
                driver.manage().addCookie(temp);
            }
            driver.get(web_url);
            driver.navigate().refresh();

            WebDriverWait wait = new WebDriverWait(driver,60);
            wait.until(presenceOfElementLocated(By.cssSelector(".u-edu-h5player-controlwrap.j-controlwrap")));
            //获取播放器
            WebElement player = driver.findElement(By.cssSelector(".u-edu-h5player-controlwrap.j-controlwrap"));
            //暂停播放按钮
            WebElement player_button = player.findElement(By.cssSelector(".u-edu-h5player-icon.icon-play"));
            //播放进度
            WebElement now_time = player.findElement(By.cssSelector(".current_time.j-current_time"));
            String all_time = player.findElement(By.cssSelector(".duration.j-duration")).getText();
            try {
                Thread.sleep(500);
            }catch (InterruptedException ex){
                System.out.println(1);
            }
            //取消自动下一个
            driver.findElement(By.cssSelector(".j-autoNext")).click();
            //没播放完就一直检测
            while(!Objects.equals(now_time.getText(),all_time) && !now_time.getText().isEmpty()){
                System.out.println("doing" + now_time.getText() + ' ' + all_time);
                System.out.println(now_time.getText());
                if(!player.findElements(By.cssSelector(".bigplaybtn.j-bigplaybtn.z-show")).isEmpty())
                {
                    if(!driver.findElements(By.cssSelector(".u-btn.u-btn-default.submit.j-submit")).isEmpty()){
                        WebElement continue_1 = driver.findElement(By.cssSelector(".u-btn.u-btn-default.submit.j-submit"));
                        WebElement continue_2 = driver.findElement(By.cssSelector(".u-btn.u-btn-default.cont.j-continue"));
                        continue_1.click();
                        continue_2.click();
                        try {
                            Thread.sleep(500);
                        }catch (InterruptedException ex){
                            System.out.println(1);
                        }

                    }
                    player_button.click();

                }
                try {
                    Thread.sleep(30000);
                }catch (InterruptedException ex){
                    System.out.println(1);
                }

            }
            driver.quit();
        }
    }
    static class Finish_3 extends Thread{
        String web_url;
        Set<Cookie> cookies;

        public Finish_3(String web_url,Set<Cookie> cookies){
            this.web_url = web_url;
            this.cookies = cookies;
        }
        @Override
        public void run(){

            WebDriver driver = new ChromeDriver();

            driver.get("https://www.icourse163.org");
            for (Cookie temp : cookies){
                driver.manage().addCookie(temp);
            }
            driver.get(web_url);
            driver.navigate().refresh();

            try {
                Thread.sleep(sleepTime);
            }catch (InterruptedException ex){
                System.out.println(1);
            }
            WebElement content = driver.findElements(By.cssSelector(".f-richEditorText.j-content")).get(1);
            StringBuilder result = new StringBuilder();
            if (content.findElements(By.cssSelector("p")).isEmpty())
                result = new StringBuilder(content.getText());
            else {
                List<WebElement>elements = content.findElements(By.cssSelector("p"));
                for (WebElement temp : elements){
                    result.append(temp.getText());
                }
            }

            driver.switchTo().frame("ueditor_0");
            WebElement input = driver.findElement(By.cssSelector("[contenteditable=\"true\"],[spellcheck=\"false\"]"));

            input.click();
            input.sendKeys(result);
            driver.switchTo().defaultContent();

            WebElement send = driver.findElement(By.cssSelector(".j-edit-btn.editbtn.u-btn.u-btn-sm.u-btn-primary.f-fr"));
            send.click();

            try{
                Thread.sleep(sleepTime);
            }catch (InterruptedException ex){
                System.out.println(1);
            }
            driver.quit();
        }

    }
    static class Finish_4 extends Thread{
        String web_url;
        Set<Cookie> cookies;

        public Finish_4(String web_url,Set<Cookie> cookies){
            this.web_url = web_url;
            this.cookies = cookies;
        }
        @Override
        public void run(){

            WebDriver driver = new ChromeDriver();

//            System.out.println(cid);
//            System.out.println(String.format("window.open(\"%s\")",web_url.getLesson_link(cid)));

//            System.out.println(1);
            driver.get("https://www.icourse163.org");
            for (Cookie temp : cookies){
                driver.manage().addCookie(temp);
            }
            driver.get(web_url);
            driver.navigate().refresh();
            try {
                Thread.sleep(sleepTime);
            }catch (InterruptedException ex){
                System.out.println(1);
            }
            driver.close();
        }
    }

}