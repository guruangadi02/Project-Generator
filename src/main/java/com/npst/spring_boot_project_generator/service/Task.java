package com.npst.spring_boot_project_generator.service;

import com.npst.spring_boot_project_generator.model.ResourceModel;

public interface Task {
    void execute(ResourceModel resourceModel);
}
