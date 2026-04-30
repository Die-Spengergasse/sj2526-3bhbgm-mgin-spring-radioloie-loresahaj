package at.spengergasse.spring_thymeleaf.controller;

import at.spengergasse.spring_thymeleaf.entities.Device;
import at.spengergasse.spring_thymeleaf.entities.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/device")
public class DeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("devices", deviceRepository.findAll());
        return "devicelist";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("device", new Device());
        return "add_device";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Device device) {
        deviceRepository.save(device);
        return "redirect:/device/list";
    }
}