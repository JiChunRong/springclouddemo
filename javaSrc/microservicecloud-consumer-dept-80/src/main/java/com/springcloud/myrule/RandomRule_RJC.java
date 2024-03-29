package com.springcloud.myrule;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

public class RandomRule_RJC extends AbstractLoadBalancerRule {

    // 依旧轮询策略， 每个服务器要求调用5次
    //  total = 0 //当前对外提供服务的服务器地址
    // index = 0 //当前对外提供服务的服务器地址
    //  total 需要重新值为零  但是依旧达到过一个5次  就重置为0  我们index  = 1
    //  总共5次  8001  8002  8003  总共3台  超过就设置为0

    private int total = 0;   //  总共被调用的次数， 目前要求每台调用5次
    private int currentIndex = 5;  // 当前提供服务的机器号

    /**
     * Randomly choose from all living servers
     */
    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            return null;
        }
        Server server = null;

        while (server == null) {
            if (Thread.interrupted()) {
                return null;
            }
            List<Server> upList = lb.getReachableServers();
            List<Server> allList = lb.getAllServers();

            int serverCount = allList.size();
            if (serverCount == 0) {
                /*
                 * No servers. End regardless of pass, because subsequent passes
                 * only get more restrictive.
                 */
                return null;
            }

//            int index = chooseRandomInt(chooseRandomIntserverCount);
//            server = upList.get(index);
            if (total < 5){
                total++;
            }else {
                total = 0;
                currentIndex++;
                if (currentIndex > upList.size() - 1){
                    currentIndex = 0;
                }
            }
            server = upList.get(currentIndex);
            if (server == null) {
                /*
                 * The only time this should happen is if the server list were
                 * somehow trimmed. This is a transient condition. Retry after
                 * yielding.
                 */
                Thread.yield();
                continue;
            }

            if (server.isAlive()) {
                return (server);
            }

            // Shouldn't actually happen.. but must be transient or a bug.
            server = null;
            Thread.yield();
        }

        return server;

    }

    protected int chooseRandomInt(int serverCount) {
        return ThreadLocalRandom.current().nextInt(serverCount);
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }
}