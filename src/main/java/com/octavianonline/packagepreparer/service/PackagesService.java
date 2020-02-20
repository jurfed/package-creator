package com.octavianonline.packagepreparer.service;

import com.octavianonline.packagepreparer.utils.Packages;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class PackagesService {

    @Bean
    Packages getPackages(){
        return new Packages();
    }

}
