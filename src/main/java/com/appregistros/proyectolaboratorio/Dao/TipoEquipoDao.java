package com.appregistros.proyectolaboratorio.Dao;

import com.appregistros.proyectolaboratorio.database.JPAUtil;
import com.appregistros.proyectolaboratorio.model.TipoEquipo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;

public class TipoEquipoDao {

    public void guardar(TipoEquipo tipoEquipo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (tipoEquipo.getIdTipo() == null) {
                em.persist(tipoEquipo);
            } else {
                em.merge(tipoEquipo);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public TipoEquipo buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(TipoEquipo.class, id);
        } finally {
            em.close();
        }
    }

    public List<TipoEquipo> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT t FROM TipoEquipo t ORDER BY t.idTipo", TipoEquipo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public TipoEquipo buscarPorNombre(String nombre) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT t FROM TipoEquipo t WHERE t.nombre = :nombre", TipoEquipo.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public void eliminar(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            TipoEquipo tipoEquipo = em.find(TipoEquipo.class, id);
            if (tipoEquipo != null) {
                em.remove(tipoEquipo);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
