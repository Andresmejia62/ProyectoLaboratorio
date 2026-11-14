package com.appregistros.proyectolaboratorio.Dao;

import com.appregistros.proyectolaboratorio.database.JPAUtil;
import com.appregistros.proyectolaboratorio.model.Equipo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;

public class EquipoDao {

    public void guardar(Equipo equipo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (equipo.getIdEquipo() == null) {
                em.persist(equipo);
            } else {
                em.merge(equipo);
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

    public Equipo buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Equipo.class, id);
        } finally {
            em.close();
        }
    }

    public List<Equipo> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT e FROM Equipo e ORDER BY e.idEquipo", Equipo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Equipo buscarPorCodigo(String codigo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT e FROM Equipo e WHERE e.codigo = :codigo", Equipo.class)
                    .setParameter("codigo", codigo)
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
            Equipo equipo = em.find(Equipo.class, id);
            if (equipo != null) {
                em.remove(equipo);
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

    public List<Equipo> buscarPorTipo(Long idTipo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT e FROM Equipo e WHERE e.tipoEquipo.idTipo = :idTipo ORDER BY e.idEquipo",
                            Equipo.class)
                    .setParameter("idTipo", idTipo)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
