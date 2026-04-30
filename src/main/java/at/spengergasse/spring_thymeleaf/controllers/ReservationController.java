package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.Reservation;
import at.spengergasse.spring_thymeleaf.entities.ReservationRepository;
import at.spengergasse.spring_thymeleaf.entities.PatientRepository;
import at.spengergasse.spring_thymeleaf.entities.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @GetMapping("/list")
    public String list(@RequestParam(required = false) Integer deviceId, Model model) {
        try {
            if (deviceId != null) {
                model.addAttribute("reservations", reservationRepository.findByDeviceId(deviceId));
                model.addAttribute("selectedDevice", deviceId);
            } else {
                model.addAttribute("reservations", reservationRepository.findAll());
                model.addAttribute("selectedDevice", null);
            }
            model.addAttribute("devices", deviceRepository.findAll());
        } catch (DataAccessException e) {
            model.addAttribute("errorMessage",
                    "Datenbankfehler: MySQL ist nicht erreichbar. Bitte starten Sie den Datenbankserver.");
            model.addAttribute("reservations", java.util.Collections.emptyList());
            model.addAttribute("devices", java.util.Collections.emptyList());
        }
        return "reservation_list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        try {
            model.addAttribute("reservation", new Reservation());
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("devices", deviceRepository.findAll());
        } catch (DataAccessException e) {
            model.addAttribute("errorMessage",
                    "Datenbankfehler: MySQL ist nicht erreichbar. Bitte starten Sie den Datenbankserver.");
            model.addAttribute("patients", java.util.Collections.emptyList());
            model.addAttribute("devices", java.util.Collections.emptyList());
        }
        return "add_reservations";
    }

    @PostMapping("/save")
    public String save(@RequestParam int patientId,
                       @RequestParam int deviceId,
                       @RequestParam String startTime,
                       @RequestParam String endTime,
                       Model model) {

        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end   = LocalDateTime.parse(endTime);

        // Hilfsmethode: Model mit Dropdowns befüllen für Fehlerfall
        try {
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("devices",  deviceRepository.findAll());
        } catch (DataAccessException e) {
            model.addAttribute("errorMessage",
                    "Datenbankfehler: MySQL ist nicht erreichbar. Bitte starten Sie den Datenbankserver.");
            model.addAttribute("patients", java.util.Collections.emptyList());
            model.addAttribute("devices",  java.util.Collections.emptyList());
            return "add_reservations";
        }

        // 1) Termin in der Vergangenheit
        if (start.isBefore(LocalDateTime.now())) {
            model.addAttribute("errorMessage",
                    "Ein Termin in der Vergangenheit kann nicht reserviert werden.");
            return "add_reservations";
        }

        // 2) Endzeit muss nach Startzeit liegen
        if (!end.isAfter(start)) {
            model.addAttribute("errorMessage",
                    "Die Endzeit muss nach der Startzeit liegen.");
            return "add_reservations";
        }

        // 3) Überschneidung beim Gerät prüfen
        if (!reservationRepository.findOverlappingByDevice(deviceId, start, end).isEmpty()) {
            model.addAttribute("errorMessage",
                    "Das gewählte Gerät ist in diesem Zeitraum bereits belegt. Bitte wählen Sie eine andere Zeit.");
            return "add_reservations";
        }

        // 4) Überschneidung beim Patienten prüfen
        if (!reservationRepository.findOverlappingByPatient(patientId, start, end).isEmpty()) {
            model.addAttribute("errorMessage",
                    "Der gewählte Patient hat in diesem Zeitraum bereits einen Termin. Bitte wählen Sie eine andere Zeit.");
            return "add_reservations";
        }

        // 5) Speichern
        try {
            Reservation r = new Reservation();
            r.setPatient(patientRepository.findById(patientId).orElseThrow());
            r.setDevice(deviceRepository.findById(deviceId).orElseThrow());
            r.setStartTime(start);
            r.setEndTime(end);
            reservationRepository.save(r);
        } catch (DataAccessException e) {
            model.addAttribute("errorMessage",
                    "Datenbankfehler: Reservierung konnte nicht gespeichert werden. Bitte starten Sie MySQL.");
            return "add_reservations";
        }

        return "redirect:/reservation/list";
    }
}