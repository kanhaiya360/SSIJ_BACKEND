package com.akeshya.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
	 @CreationTimestamp
	    @Column(name = "created_date", updatable = false)
	    private LocalDateTime createdDate;

	    @UpdateTimestamp
	    @Column(name = "updated_date",insertable = false)
	    private LocalDateTime updatedDate;

	    public LocalDateTime getCreatedDate() {
	        return createdDate;
	    }

	    public LocalDateTime getUpdatedDate() {
	        return updatedDate;
	    }

}
