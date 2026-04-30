package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.Patient;
import at.spengergasse.spring_thymeleaf.entities.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("patients", patientRepository.findAll());
        } catch (DataAccessException e) {
            model.addAttribute("errorMessage",
                    "Datenbankfehler: MySQL ist nicht erreichbar. Bitte starten Sie den Datenbankserver.");
            model.addAttribute("patients", java.util.Collections.emptyList());
        }
        return "patlist";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "add_patient";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Patient patient, Model model) {

        // 1) Geburtsdatum darf nicht in der Zukunft liegen
        if (patient.getBirth() != null && patient.getBirth().isAfter(LocalDate.now())) {
            model.addAttribute("patient", patient);
            model.addAttribute("errorMessage",
                    "Das Geburtsdatum darf nicht in der Zukunft liegen.");
            return "add_patient";
        }

        // 2) Sozialversicherungsnummer: genau 10 Ziffern
        if (patient.getSsn() == null || !patient.getSsn().matches("\\d{10}")) {
            model.addAttribute("patient", patient);
            model.addAttribute("errorMessage",
                    "Die Sozialversicherungsnummer muss genau 10 Ziffern enthalten (z.B. 1234010190).");
            return "add_patient";
        }

        // 3) Datenbank speichern
        try {
            patientRepository.save(patient);
        } catch (DataAccessException e) {
            model.addAttribute("patient", patient);
            model.addAttribute("errorMessage",
                    "Datenbankfehler: Patient konnte nicht gespeichert werden. Bitte starten Sie MySQL.");
            return "add_patient";
        }

        return "redirect:/patient/list";
    }
}