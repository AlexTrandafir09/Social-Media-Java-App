package com.socialmediaapp.contentservice.content.service;

import com.socialmediaapp.contentservice.content.entity.PostImage;
import com.socialmediaapp.contentservice.content.exception.PostImageNotFoundException;
import com.socialmediaapp.contentservice.content.repository.PostImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostImageService {

    private final PostImageRepository postImageRepository;

    @Transactional(readOnly = true)
    public PostImage getImageById(Long id) {
        return postImageRepository.findById(id)
                .orElseThrow(() -> new PostImageNotFoundException(id));
    }
}
