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
            return em.createQuery(
                            "SELECT e FROM Equipo e JOIN FETCH e.tipoEquipo t ORDER BY e.idEquipo",
                            Equipo.class)
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
                            "SELECT e FROM Equipo e JOIN FETCH e.tipoEquipo t WHERE t.idTipo = :idTipo ORDER BY e.idEquipo",
                            Equipo.class)
                    .setParameter("idTipo", idTipo)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Equipo> buscarPorFiltros(String nombre, Long idTipo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT e FROM Equipo e JOIN FETCH e.tipoEquipo t WHERE 1 = 1");

            if (nombre != null && !nombre.trim().isEmpty()) {
                jpql.append(" AND LOWER(e.nombre) LIKE LOWER(:nombre)");
            }
            if (idTipo != null) {
                jpql.append(" AND t.idTipo = :idTipo");
            }
            jpql.append(" ORDER BY e.idEquipo");

            var query = em.createQuery(jpql.toString(), Equipo.class);

            if (nombre != null && !nombre.trim().isEmpty()) {
                query.setParameter("nombre", "%" + nombre.trim() + "%");
            }
            if (idTipo != null) {
                query.setParameter("idTipo", idTipo);
            }

            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
