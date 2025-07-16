package com.minijuegos.plataforma.minijuegos_backend.controller;

import com.minijuegos.plataforma.minijuegos_backend.model.Minijuego;
import com.minijuegos.plataforma.minijuegos_backend.repository.MinijuegoRepository;

import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; // Añade RequestMapping, GetMapping, PostMapping, PathVariable
import org.springframework.web.multipart.MultipartFile; // PARA LA SUBIDA DE ARCHIVOS
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Para mensajes flash
import jakarta.validation.Valid; // Necesaria para que @Valid funcione
import org.springframework.validation.BindingResult;
import java.util.zip.ZipEntry; // Añade esta importación
import java.util.zip.ZipInputStream; // Añade esta importación
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Para generar nombres únicos para archivos

@Controller
@RequestMapping("/admin") // Todas las URLs de este controlador comienzan con /admin
public class AdminController {

    @Autowired
    private MinijuegoRepository minijuegoRepository;
    // Ruta base donde se guardarán los archivos estáticos (dentro de src/main/resources/static)
    private static final String UPLOAD_DIR = "src/main/resources/static/";
    private static final String IMAGES_SUBDIR = "images/"; // Para portadas
    private static final String MINIJUEGOS_SUBDIR = "minijuegos/";


    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        long totalMinijuegos = minijuegoRepository.count(); // Obtiene el número total de minijuegos
        model.addAttribute("totalMinijuegos", totalMinijuegos);
        return "admin/dashboard"; // Retorna la plantilla src/main/resources/templates/admin/dashboard.html
    }

    // Este controlador gestionará las operaciones CRUD de Minijuegos para el administrador
    @GetMapping("/minijuegos")
    public String gestionarMinijuegos(Model model) {
        List<Minijuego> minijuegos = minijuegoRepository.findAll();
        model.addAttribute("minijuegos", minijuegos);
        return "admin/list-minijuegos"; // Crearemos esta plantilla en el siguiente paso
    }

    // ... (código existente) ...

    // Muestra el formulario para añadir un nuevo minijuego
    @GetMapping("/minijuegos/new")
    public String showAddMinijuegoForm(Model model) {
        model.addAttribute("minijuego", new Minijuego()); // Crea un nuevo objeto Minijuego vacío
        return "admin/minijuego-form";
    }

    // Muestra el formulario para editar un minijuego existente
    @GetMapping("/minijuegos/edit/{id}")
    public String showEditMinijuegoForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Minijuego> minijuegoOptional = minijuegoRepository.findById(id);
        if (minijuegoOptional.isPresent()) {
            model.addAttribute("minijuego", minijuegoOptional.get());
            return "admin/minijuego-form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Minijuego no encontrado.");
            return "redirect:/admin/minijuegos";
        }
    }

    // Procesa el envío del formulario para guardar o actualizar un minijuego
    @PostMapping({"/minijuegos/save", "/minijuegos/save/{id}"})
    public String saveMinijuego(@Valid @ModelAttribute Minijuego minijuego,
                                BindingResult bindingResult,
                                @RequestParam("urlPortadaFile") MultipartFile urlPortadaFile,
                                @RequestParam("rutaJuegoFile") MultipartFile rutaJuegoFile,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/minijuego-form";
        }
        try {
            // Manejar la imagen de portada (sin cambios, déjala como está)
            if (!urlPortadaFile.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + urlPortadaFile.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + IMAGES_SUBDIR, fileName);
                Files.createDirectories(filePath.getParent());
                Files.copy(urlPortadaFile.getInputStream(), filePath);
                minijuego.setUrlPortada("/" + IMAGES_SUBDIR + fileName);
            } else if (minijuego.getId() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Debe subir una imagen de portada para un nuevo minijuego.");
                return "redirect:/admin/minijuegos/new";
            }

            // --- INICIO DE LA LÓGICA DE DESCOMPRESIÓN DEL ZIP ---
            if (!rutaJuegoFile.isEmpty()) {
                // Genera un nombre de carpeta único para el juego
                String gameFolderName = UUID.randomUUID().toString();
                Path gameFolderPath = Paths.get(UPLOAD_DIR + MINIJUEGOS_SUBDIR, gameFolderName);
                Files.createDirectories(gameFolderPath); // Crea la carpeta del juego

                // Abre el ZIP para lectura
                try (InputStream is = rutaJuegoFile.getInputStream();
                     ZipInputStream zis = new ZipInputStream(is)) {

                    ZipEntry zipEntry = zis.getNextEntry();
                    boolean indexHtmlFound = false; // Bandera para verificar si hay un index.html

                    while (zipEntry != null) {
                        Path newFilePath = gameFolderPath.resolve(zipEntry.getName());

                        // Seguridad: Evitar Path Traversal (evitar que el ZIP escriba fuera de la carpeta)
                        if (!newFilePath.normalize().startsWith(gameFolderPath.normalize())) {
                            throw new IOException("Entrada ZIP fuera de la carpeta de destino: " + zipEntry.getName());
                        }

                        if (zipEntry.isDirectory()) {
                            Files.createDirectories(newFilePath);
                        } else {
                            // Asegura que los directorios padre existan antes de escribir el archivo
                            Files.createDirectories(newFilePath.getParent());
                            Files.copy(zis, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                            if (zipEntry.getName().equalsIgnoreCase("index.html")) {
                                indexHtmlFound = true;
                            }
                        }
                        zipEntry = zis.getNextEntry();
                    }
                    zis.closeEntry();

                    if (!indexHtmlFound) {
                        // Opcional: Manejar si no se encuentra index.html. Puedes lanzar un error
                        // o crear uno básico como fallback. Aquí, por simplicidad, lanzaremos un error.
                        throw new IOException("El archivo ZIP del juego debe contener un 'index.html' en la raíz.");
                    }

                    // Establece la ruta final del juego en el objeto Minijuego
                    minijuego.setRutaJuego("/" + MINIJUEGOS_SUBDIR + gameFolderName + "/index.html");

                } catch (IOException e) {
                    // Si ocurre un error de descompresión, intentar limpiar la carpeta creada
                    try {
                        if (Files.exists(gameFolderPath)) {
                            Files.walk(gameFolderPath)
                                    .sorted(java.util.Comparator.reverseOrder())
                                    .map(Path::toFile)
                                    .forEach(java.io.File::delete);
                        }
                    } catch (IOException cleanupException) {
                        System.err.println("Error al limpiar la carpeta del juego después de un fallo de descompresión: " + cleanupException.getMessage());
                    }
                    throw e; // Relanza la excepción original para que el catch externo la maneje
                }
            } else if (minijuego.getId() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Debe subir un archivo de juego (ZIP) para un nuevo minijuego.");
                return "redirect:/admin/minijuegos/new";
            }
            // --- FIN DE LA LÓGICA DE DESCOMPRESIÓN DEL ZIP ---

            minijuegoRepository.save(minijuego);
            redirectAttributes.addFlashAttribute("successMessage", "Minijuego guardado correctamente.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el archivo del juego: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/minijuegos/new";
        } catch (Exception e) { // Captura cualquier otra excepción no específicamente de IO
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado al guardar el minijuego: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/minijuegos/new";
        }

        return "redirect:/admin/minijuegos";
    }


    // Eliminar un minijuego
    @PostMapping("/minijuegos/delete/{id}")
    public String deleteMinijuego(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Minijuego> minijuegoOptional = minijuegoRepository.findById(id);
        if (minijuegoOptional.isPresent()) {
            Minijuego minijuego = minijuegoOptional.get();

            try {
                // Opcional: Eliminar los archivos físicos (imagen y carpeta del juego)
                if (minijuego.getUrlPortada() != null && !minijuego.getUrlPortada().isEmpty()) {
                    Path imagePath = Paths.get(UPLOAD_DIR + minijuego.getUrlPortada().substring(1)); // Quita el '/' inicial
                    Files.deleteIfExists(imagePath);
                }
                if (minijuego.getRutaJuego() != null && !minijuego.getRutaJuego().isEmpty()) {
                    // Extrae la parte de la carpeta del juego (ej. de "minijuegos/abcd-1234/index.html" a "abcd-1234")
                    String gameFolderRelative = minijuego.getRutaJuego().replace("minijuegos/", "");
                    int firstSlashIndex = gameFolderRelative.indexOf("/");
                    if (firstSlashIndex != -1) {
                        gameFolderRelative = gameFolderRelative.substring(0, firstSlashIndex);
                    }
                    Path gameFolderPath = Paths.get(UPLOAD_DIR + MINIJUEGOS_SUBDIR + gameFolderRelative);

                    // Eliminar recursivamente la carpeta del juego
                    if (Files.exists(gameFolderPath)) {
                        Files.walk(gameFolderPath)
                                .sorted(java.util.Comparator.reverseOrder())
                                .map(Path::toFile)
                                .forEach(java.io.File::delete);
                    }
                }

                minijuegoRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Minijuego eliminado correctamente.");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar los archivos asociados: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el minijuego: " + e.getMessage());
                e.printStackTrace();
            }

        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Minijuego no encontrado.");
        }
        return "redirect:/admin/minijuegos";
    }

    // Próximamente: Métodos para añadir/editar/borrar minijuegos (GET y POST para formularios)
}