package mate.academy.dao.impl;

import java.util.List;
import java.util.Optional;
import mate.academy.dao.OrderDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.Order;
import mate.academy.model.User;
import mate.academy.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Dao
public class OrderDaoImpl implements OrderDao {
    @Override
    public Order create(Order order) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(order);
            transaction.commit();
            return order;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't create order " + order, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Optional<Order> getByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Order o "
                    + "LEFT JOIN FETCH o.tickets t "
                    + "LEFT JOIN FETCH t.movieSession ms "
                    + "LEFT JOIN ms.movie "
                    + "LEFT JOIN ms.cinemaHall "
                    + "WHERE o.user = :user", Order.class)
                    .setParameter("user", user).uniqueResultOptional();
        } catch (Exception e) {
            throw new DataProcessingException("Can't get order by user " + user.getId(), e);
        }
    }

    @Override
    public List<Order> getHistoryOfOrders(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Order o "
                            + "LEFT JOIN FETCH o.user u "
                            + "LEFT JOIN FETCH o.tickets t "
                            + "LEFT JOIN FETCH t.movieSession ms "
                            + "LEFT JOIN FETCH ms.movie "
                            + "LEFT JOIN FETCH ms.cinemaHall "
                            + "WHERE o.user = :user", Order.class)
                    .setParameter("user", user)
                    .getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Can't get history of orders for user"
                    + " with id: " + user.getId(), e);
        }
    }
}
