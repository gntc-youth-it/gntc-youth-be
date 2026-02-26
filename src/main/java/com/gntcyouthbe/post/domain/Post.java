package com.gntcyouthbe.post.domain;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.common.orm.domain.BaseEntity;
import com.gntcyouthbe.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PostSubCategory subCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 20)
    private List<PostImage> images = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "post_hashtag",
            joinColumns = @JoinColumn(name = "post_id"),
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Column(name = "hashtag", nullable = false, length = 100)
    @BatchSize(size = 20)
    private List<String> hashtags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "post_church",
            joinColumns = @JoinColumn(name = "post_id"),
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "church_id", nullable = false, length = 30)
    @BatchSize(size = 20)
    private List<ChurchId> churches = new ArrayList<>();

    public Post(User author, PostSubCategory subCategory, PostStatus status, String content) {
        this.author = author;
        this.subCategory = subCategory;
        this.status = status;
        this.content = content;
    }

    public PostCategory getCategory() {
        return subCategory.getCategory();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateStatus(PostStatus status) {
        this.status = status;
    }

    public void addImage(PostImage image) {
        images.add(image);
        image.setPost(this);
    }

    public void updateHashtags(List<String> hashtags) {
        this.hashtags.clear();
        if (hashtags != null) {
            this.hashtags.addAll(hashtags);
        }
    }

    public void updateChurches(List<ChurchId> churches) {
        this.churches.clear();
        if (churches != null) {
            this.churches.addAll(churches);
        }
    }
}
