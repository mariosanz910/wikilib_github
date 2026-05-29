package com.tfg.wikilib.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.tfg.wikilib.model.Serie;
import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.service.SerieService;
import com.tfg.wikilib.service.PublicacionService;

import java.util.List;

@Controller
public class SerieController {

    @Autowired
    private SerieService serieService;

    @Autowired
    private PublicacionService publicacionService;

    @GetMapping("/series")
    public String catalogoSeries(
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int pageSize = 12;
        Page<Serie> seriesPage = serieService.obtenerCatalogoSeries(buscar, PageRequest.of(page, pageSize));

        model.addAttribute("series", seriesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", seriesPage.getTotalPages());
        model.addAttribute("totalElements", seriesPage.getTotalElements());
        model.addAttribute("buscar", buscar);
        model.addAttribute("hasNext", seriesPage.hasNext());
        model.addAttribute("hasPrevious", seriesPage.hasPrevious());

        return "series/todas-las-series";
    }

    @GetMapping("/series/{id}")
    public String detalleSerie(@PathVariable Long id, Model model) {
        Serie serie = serieService.buscarPorId(id);
        List<Publicacion> publicaciones = publicacionService.obtenerPorSerie(serie);
        
        model.addAttribute("serie", serie);
        model.addAttribute("publicaciones", publicaciones);
        
        return "series/detalle-serie";
    }
}
