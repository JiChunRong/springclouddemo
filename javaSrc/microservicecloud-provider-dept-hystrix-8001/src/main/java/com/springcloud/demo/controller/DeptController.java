package com.springcloud.demo.controller;import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;import com.springcloud.demo.entity.Dept;import com.springcloud.demo.service.DeptService;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.cloud.client.ServiceInstance;import org.springframework.cloud.client.discovery.DiscoveryClient;import org.springframework.web.bind.annotation.*;import java.util.List;/** * \* Created with IntelliJ IDEA. * \* User: rongjichun * \* Date: 2019-04-25 8:51 * \* Description: * \ */@RestControllerpublic class DeptController {    @Autowired    private DeptService service = null;    @RequestMapping(value="/dept/get/{id}",method=RequestMethod.GET)    @HystrixCommand(fallbackMethod = "processHystrix_Get")  //一旦调用服务方法失败并抛出了错误信息后，会自动调用@HystrixCommand标注好的fallbackMethod调用类中的指定方法    public Dept get(@PathVariable("id") Long id)    {        Dept dept =  this.service.get(id);        if(null == dept)        {            throw new RuntimeException("该ID："+id+"没有没有对应的信息");        }        return dept;    }    public Dept processHystrix_Get(@PathVariable("id") Long id)    {        return new Dept().setDeptno(id)                .setDname("该ID："+id+"没有没有对应的信息,null--@HystrixCommand")                .setDb_source("no this database in MySQL");    }}