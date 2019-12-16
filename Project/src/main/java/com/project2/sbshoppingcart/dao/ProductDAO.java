package com.project2.sbshoppingcart.dao;

import java.io.IOException;
import java.util.Date;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.project2.sbshoppingcart.entity.Product;
import com.project2.sbshoppingcart.form.ProductForm;
import com.project2.sbshoppingcart.model.ProductInfo;
import com.project2.sbshoppingcart.pagination.PaginationResult;

@Transactional
@Repository
public class ProductDAO {

	@Autowired
	private SessionFactory sessionFactory;

	public Product findProduct(String code) {
		try {
			String sql = "Select e from " + Product.class.getName() + " e Where e.code =:code ";

			Session session = this.sessionFactory.getCurrentSession();
			Query<Product> query = session.createQuery(sql, Product.class); // Tim hieu HQL ?
			query.setParameter("code", code);
			return (Product) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public ProductInfo findProductInfo(String code) {
		Product product = this.findProduct(code);
		if (product == null) {
			return null;
		}
		return new ProductInfo(product.getCode(), product.getName(), product.getPrice(), product.getDescription(),
				product.getCategory());
	}

	public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage, // Tìm kiếm theo
																										// tên ..
			String likeName) {
		String sql = "Select new " + ProductInfo.class.getName() //
				+ "(p.code, p.name, p.price,p.description,p.category )" + " from " + Product.class.getName() + " p ";
		if (likeName != null && likeName.length() > 0) {
			sql += " Where lower(p.name) like :likeName ";
		}
		sql += " order by p.createDate desc ";
		//
		Session session = this.sessionFactory.getCurrentSession();
		Query<ProductInfo> query = session.createQuery(sql, ProductInfo.class);

		if (likeName != null && likeName.length() > 0) {
			query.setParameter("likeName", "%" + likeName.toLowerCase() + "%");
		}
		return new PaginationResult<ProductInfo>(query, page, maxResult, maxNavigationPage);
	}

	public PaginationResult<ProductInfo> queryProductsbyCategory(int page, int maxResult, int maxNavigationPage, // Tìm
																													// kiếm
																													// theo
																													// tên
																													// ..
			String category) {
		String sql = "Select new " + ProductInfo.class.getName() //
				+ "(p.code, p.name, p.price,p.description,p.category )" + " from " + Product.class.getName() + " p ";
		if (category != null && category.length() > 0) {
			sql += " Where lower(p.category) like :category ";
		}
		sql += " order by p.createDate desc ";
		//
		Session session = this.sessionFactory.getCurrentSession();
		Query<ProductInfo> query = session.createQuery(sql, ProductInfo.class);

		if (category != null && category.length() > 0) {
			query.setParameter("category", "%" + category.toLowerCase() + "%");
		}
		return new PaginationResult<ProductInfo>(query, page, maxResult, maxNavigationPage);
	}

	public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage) {
		return queryProducts(page, maxResult, maxNavigationPage, null);
	}

	public PaginationResult<ProductInfo> queryProductsbyName(int page, int maxResult, int maxNavigationPage,
			String likeName) {
		return queryProducts(page, maxResult, maxNavigationPage, likeName);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void save(ProductForm productForm) {

		Session session = this.sessionFactory.getCurrentSession();
		String code = productForm.getCode();

		Product product = null;

		boolean isNew = false;
		if (code != null) {
			product = this.findProduct(code);
		}
		if (product == null) {
			isNew = true;
			product = new Product();
			product.setCreateDate(new Date());
		}
		product.setCode(code);
		product.setName(productForm.getName());
		product.setPrice(productForm.getPrice());
		product.setDescription(productForm.getDescription());
		product.setCategory(productForm.getCategory());
		if (productForm.getFileData() != null) {
			byte[] image = null;
			try {
				image = productForm.getFileData().getBytes();
			} catch (IOException e) {
			}
			if (image != null && image.length > 0) {
				product.setImage(image);
			}
		}
		if (isNew) {
			session.persist(product);
		}
		// Nếu có lỗi tại DB, ngoại lệ sẽ ném ra ngay lập tức
		session.flush();
	}

	public void deleteProduct(String code) {

//		String sql = "Delete from " + Product.class.getName() + "where code = :code";

		Session session = sessionFactory.getCurrentSession();
		
		Product product = findProduct(code);
		session.remove(product);
//		Query<Product> query = session.createQuery(sql, Product.class);
//		query.setParameter("code", productForm.getCode());

	}

}