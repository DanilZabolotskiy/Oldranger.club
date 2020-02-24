package ru.java.mentor.oldranger.club.restcontroller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.java.mentor.oldranger.club.dao.MediaRepository.PhotoAlbumRepository;
import ru.java.mentor.oldranger.club.dao.MediaRepository.PhotoRepository;
import ru.java.mentor.oldranger.club.dto.PhotoAndCommentsDTO;
import ru.java.mentor.oldranger.club.dto.PhotoCommentDto;
import ru.java.mentor.oldranger.club.model.media.Photo;
import ru.java.mentor.oldranger.club.model.media.PhotoAlbum;
import ru.java.mentor.oldranger.club.model.user.User;
import ru.java.mentor.oldranger.club.service.media.PhotoAlbumService;
import ru.java.mentor.oldranger.club.service.media.PhotoService;
import ru.java.mentor.oldranger.club.service.utils.SecurityUtilsService;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/photos")
@Tag(name = "Photo")
public class PhotoRestController {

    private PhotoService service;
    private PhotoAlbumService albumService;
    private SecurityUtilsService securityUtilsService;
    private PhotoAlbumRepository photoAlbumRepository;
    private PhotoRepository photoRepository;

    @Operation(security = @SecurityRequirement(name = "security"),
            summary = "Save photo in album", tags = {"Photo"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Photo.class)))),
            @ApiResponse(responseCode = "400", description = "Rights error")})
    @RequestMapping(value = "/{albumId}", method = RequestMethod.POST)
    public ResponseEntity<List<Photo>> savePhoto(@RequestBody List<MultipartFile> photos, @PathVariable("albumId") String albumId) {
        User currentUser = securityUtilsService.getLoggedUser();
        PhotoAlbum photoAlbum = albumService.findById(Long.parseLong(albumId));
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!photoAlbum.getWriters().contains(currentUser) && !securityUtilsService.isAdmin() ||
                !securityUtilsService.isModerator() && photoAlbum.getWriters().size() != 0) {
            return ResponseEntity.badRequest().build();
        }
        List<Photo> savedPhotos = new ArrayList<>();
        photos.forEach(a -> savedPhotos.add(service.save(photoAlbum, a)));

        if (photoAlbum.getThumbImage() == null) {
            photoAlbum.setThumbImage(savedPhotos.get(0));
            photoAlbumRepository.save(photoAlbum);
        }
        return ResponseEntity.ok(savedPhotos);
    }

    @Operation(security = @SecurityRequirement(name = "security"),
            summary = "Get photo and a list of comments DTO by id", tags = {"Photo"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PhotoCommentDto.class)))),
            @ApiResponse(responseCode = "400", description = "Error id or rights error")})
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<PhotoAndCommentsDTO> getPhoto(@PathVariable("id") String id,
                                                        @RequestParam(value = "page", required = false) Integer page,
                                                        @RequestParam(value = "pos", required = false) Integer position,
                                                        @RequestParam(value = "limit", required = false) Integer limit) {
        User currentUser = securityUtilsService.getLoggedUser();
        Photo photo = service.findById(Long.parseLong(id));
        if (photo == null) {
            return ResponseEntity.badRequest().build();
        }
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        PhotoAlbum photoAlbum = photo.getAlbum();
        if (!photoAlbum.getViewers().contains(currentUser) && !securityUtilsService.isAdmin() ||
                !securityUtilsService.isModerator() && photoAlbum.getViewers().size() != 0) {
            return ResponseEntity.badRequest().build();
        }
        if (limit == null) {
            limit = 10;
        }
        if (page == null) page = 0;
        Pageable pageable = PageRequest.of(page, limit, Sort.by("dateTime"));
        if (position == null) {
            position = 0;
        }
        Page<PhotoCommentDto> dtos = service.getPageableCommentDtoByPhoto(photo, pageable, position);
        PhotoAndCommentsDTO photoCommentsDto = new PhotoAndCommentsDTO(photo, dtos);
        return ResponseEntity.ok(photoCommentsDto);
    }

    @Operation(security = @SecurityRequirement(name = "security"),
            summary = "Update photo", tags = {"Photo"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Photo.class)))),
            @ApiResponse(responseCode = "400", description = "Rights error")})
    @RequestMapping(value = "/{photoId}", method = RequestMethod.PUT)
    public ResponseEntity<Photo> updatePhoto(@RequestBody MultipartFile newPhoto, @PathVariable("photoId") String photoId) {
        User currentUser = securityUtilsService.getLoggedUser();
        Photo photo = service.findById(Long.valueOf(photoId));
        if (photo == null || newPhoto == null) {
            return ResponseEntity.badRequest().build();
        }
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        PhotoAlbum photoAlbum = photo.getAlbum();
        if (!photoAlbum.getWriters().contains(currentUser) && !securityUtilsService.isAdmin() ||
                !securityUtilsService.isModerator() && photoAlbum.getWriters().size() != 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.update(newPhoto, photo));
    }

    @Operation(security = @SecurityRequirement(name = "security"),
            summary = "Delete photo", tags = {"Photo"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "Error id or rights error")})
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deletePhoto(@PathVariable("id") String id) {
        User currentUser = securityUtilsService.getLoggedUser();
        Photo photo = service.findById(Long.parseLong(id));
        if (photo == null) {
            return ResponseEntity.badRequest().build();
        }
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        PhotoAlbum photoAlbum = photo.getAlbum();
        if (!photoAlbum.getWriters().contains(currentUser) && !securityUtilsService.isAdmin() ||
                !securityUtilsService.isModerator() && photoAlbum.getWriters().size() != 0) {
            return ResponseEntity.badRequest().build();
        }
        service.deletePhoto(Long.parseLong(id));
        if (photoAlbum.getThumbImage().getId().equals(photo.getId())) {
            List<Photo> photoList = photoRepository.findAllByAlbum(photoAlbum);
            if (photoList.size() != 0) {
                photoAlbum.setThumbImage(photoList.get(0));
            } else {
                photoAlbum.setThumbImage(null);
            }
            photoAlbumRepository.save(photoAlbum);
        }
        return ResponseEntity.ok("delete ok");
    }
}