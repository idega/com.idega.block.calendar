/**
 * @(#)AttendeeDAOImpl.java    1.0.0 12:59:01
 *
 * Idega Software hf. Source Code Licence Agreement x
 *
 * This agreement, made this 10th of February 2006 by and between 
 * Idega Software hf., a business formed and operating under laws 
 * of Iceland, having its principal place of business in Reykjavik, 
 * Iceland, hereinafter after referred to as "Manufacturer" and Agura 
 * IT hereinafter referred to as "Licensee".
 * 1.  License Grant: Upon completion of this agreement, the source 
 *     code that may be made available according to the documentation for 
 *     a particular software product (Software) from Manufacturer 
 *     (Source Code) shall be provided to Licensee, provided that 
 *     (1) funds have been received for payment of the License for Software and 
 *     (2) the appropriate License has been purchased as stated in the 
 *     documentation for Software. As used in this License Agreement, 
 *     Licensee shall also mean the individual using or installing 
 *     the source code together with any individual or entity, including 
 *     but not limited to your employer, on whose behalf you are acting 
 *     in using or installing the Source Code. By completing this agreement, 
 *     Licensee agrees to be bound by the terms and conditions of this Source 
 *     Code License Agreement. This Source Code License Agreement shall 
 *     be an extension of the Software License Agreement for the associated 
 *     product. No additional amendment or modification shall be made 
 *     to this Agreement except in writing signed by Licensee and 
 *     Manufacturer. This Agreement is effective indefinitely and once
 *     completed, cannot be terminated. Manufacturer hereby grants to 
 *     Licensee a non-transferable, worldwide license during the term of 
 *     this Agreement to use the Source Code for the associated product 
 *     purchased. In the event the Software License Agreement to the 
 *     associated product is terminated; (1) Licensee's rights to use 
 *     the Source Code are revoked and (2) Licensee shall destroy all 
 *     copies of the Source Code including any Source Code used in 
 *     Licensee's applications.
 * 2.  License Limitations
 *     2.1 Licensee may not resell, rent, lease or distribute the 
 *         Source Code alone, it shall only be distributed as a 
 *         compiled component of an application.
 *     2.2 Licensee shall protect and keep secure all Source Code 
 *         provided by this this Source Code License Agreement. 
 *         All Source Code provided by this Agreement that is used 
 *         with an application that is distributed or accessible outside
 *         Licensee's organization (including use from the Internet), 
 *         must be protected to the extent that it cannot be easily 
 *         extracted or decompiled.
 *     2.3 The Licensee shall not resell, rent, lease or distribute 
 *         the products created from the Source Code in any way that 
 *         would compete with Idega Software.
 *     2.4 Manufacturer's copyright notices may not be removed from 
 *         the Source Code.
 *     2.5 All modifications on the source code by Licencee must 
 *         be submitted to or provided to Manufacturer.
 * 3.  Copyright: Manufacturer's source code is copyrighted and contains 
 *     proprietary information. Licensee shall not distribute or 
 *     reveal the Source Code to anyone other than the software 
 *     developers of Licensee's organization. Licensee may be held 
 *     legally responsible for any infringement of intellectual property 
 *     rights that is caused or encouraged by Licensee's failure to abide 
 *     by the terms of this Agreement. Licensee may make copies of the 
 *     Source Code provided the copyright and trademark notices are 
 *     reproduced in their entirety on the copy. Manufacturer reserves 
 *     all rights not specifically granted to Licensee.
 *
 * 4.  Warranty & Risks: Although efforts have been made to assure that the 
 *     Source Code is correct, reliable, date compliant, and technically 
 *     accurate, the Source Code is licensed to Licensee as is and without 
 *     warranties as to performance of merchantability, fitness for a 
 *     particular purpose or use, or any other warranties whether 
 *     expressed or implied. Licensee's organization and all users 
 *     of the source code assume all risks when using it. The manufacturers, 
 *     distributors and resellers of the Source Code shall not be liable 
 *     for any consequential, incidental, punitive or special damages 
 *     arising out of the use of or inability to use the source code or 
 *     the provision of or failure to provide support services, even if we 
 *     have been advised of the possibility of such damages. In any case, 
 *     the entire liability under any provision of this agreement shall be 
 *     limited to the greater of the amount actually paid by Licensee for the 
 *     Software or 5.00 USD. No returns will be provided for the associated 
 *     License that was purchased to become eligible to receive the Source 
 *     Code after Licensee receives the source code. 
 */
package com.idega.block.calendar.data.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.calendar.data.AttendeeEntity;
import com.idega.block.calendar.data.dao.AttendeeDAO;
import com.idega.core.persistence.Param;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.data.SimpleQuerier;
import com.idega.user.dao.UserDAO;
import com.idega.user.data.bean.User;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

/**
 * <p>You can report about problems to: 
 * <a href="mailto:martynas@idega.is">Martynas Stakė</a></p>
 *
 * @version 1.0.0 2015 gruod. 18
 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
 */
@Repository("attendeeDAO")
@Transactional(readOnly = false)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AttendeeDAOImpl extends GenericDaoImpl implements AttendeeDAO {

	@Autowired
	private UserDAO userDAO;

	private UserDAO getUserDAO() {
		if (this.userDAO == null) {
			ELUtil.getInstance().autowire(this);
		}

		return this.userDAO;
	}

	/* (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#update(com.idega.block.calendar.data.AttendeeEntity)
	 */
	@Override
	public AttendeeEntity update(AttendeeEntity entity) {
		if (entity != null) {
			if (entity.getId() == null) {
				persist(entity);
				if (entity.getId() != null) {
					return entity;
				} else {
					getLogger().warning("Failed to save entity");
				}
			} else {
				return merge(entity);
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#update(java.lang.Long, java.lang.Integer, com.idega.user.data.bean.User, com.idega.user.data.bean.User)
	 */
	@Override
	public AttendeeEntity update(Long id, Integer groupId, User inviter,
			User invitee) {
		AttendeeEntity entity = findByPrimaryKey(id);
		if (entity == null) {
			entity = findBy(inviter, invitee, groupId);
		} 

		if (entity == null) {
			entity = new AttendeeEntity();
		}

		if (groupId != null) {
			entity.setEventGroupId(groupId);
		}

		if (invitee != null) {
			entity.setInvitee(invitee);
		}

		if (inviter != null) {
			entity.setInviter(inviter);
		}

		return update(entity);
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#update(
	 * 		java.lang.Long, 
	 * 		java.lang.Integer, 
	 * 		com.idega.user.data.bean.User, 
	 * 		java.util.Collection
	 * 	)
	 */
	@Override
	public List<AttendeeEntity> update(
			Integer groupId, 
			User inviter,
			Collection<User> invitees) {
		ArrayList<AttendeeEntity> entities = new ArrayList<AttendeeEntity>();

		if (!ListUtil.isEmpty(invitees)) {
			for (User invitee : invitees) {
				AttendeeEntity entity = update(null, groupId, inviter, invitee);
				if (entity != null) {
					entities.add(entity);
				}
			}
		}

		return entities;
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#update(java.lang.Integer, java.lang.Integer, java.util.Collection)
	 */
	@Override
	public List<AttendeeEntity> update(
			Integer groupId,
			Integer inviterPrimaryKey, 
			Collection<Integer> inviteesPrimaryKeys) {

		return update(groupId,
				getUserDAO().getUser(inviterPrimaryKey),
				getUserDAO().findAll(inviteesPrimaryKeys));
	}

	/* (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#remove(java.lang.Long)
	 */
	@Override
	public void remove(Long id) {
		AttendeeEntity entity = findByPrimaryKey(id);
		if (entity != null) {
			remove(entity);
		}

	}

	/* (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#removeByEventGroup(java.lang.Integer)
	 */
	@Override
	public void removeByEventGroup(Integer id) {
		List<AttendeeEntity> entities = findByEventGroupId(id);
		for (AttendeeEntity entity: entities) {
			remove(entity.getId());
		}
	}

	/* (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#findByPrimaryKey(java.lang.Long)
	 */
	@Override
	public AttendeeEntity findByPrimaryKey(Long id) {
		if (id != null) {
			return getSingleResult(
					AttendeeEntity.FIND_BY_ID,
					AttendeeEntity.class, 
					new Param(AttendeeEntity.idProp, id));
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#findByPrimaryKeys(java.util.Collection)
	 */
	@Override
	public List<AttendeeEntity> findByPrimaryKeys(Collection<Long> primaryKeys) {
		if (!ListUtil.isEmpty(primaryKeys)) {
			return getResultList(
					AttendeeEntity.FIND_BY_PRIMARY_KEYS,
					AttendeeEntity.class,
					new Param(AttendeeEntity.idProp, primaryKeys));
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#findByEventGroupId(java.lang.Integer)
	 */
	@Override
	public List<AttendeeEntity> findByEventGroupId(Integer groupId) {
		if (groupId != null) {
			return getResultList(
					AttendeeEntity.FIND_BY_EVENT_GROUP_ID,
					AttendeeEntity.class,
					new Param(AttendeeEntity.eventGroupIdProp, groupId));
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#findByInviter(com.idega.user.data.bean.User)
	 */
	@Override
	public List<AttendeeEntity> findByInviter(User inviter) {
		if (inviter != null) {
			return getResultList(
					AttendeeEntity.FIND_BY_INVITER_ID,
					AttendeeEntity.class,
					new Param(AttendeeEntity.inviterProp, inviter));
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#findByInvited(com.idega.user.data.bean.User)
	 */
	@Override
	public List<AttendeeEntity> findByInvitee(User invitee) {
		if (invitee != null) {
			return getResultList(
					AttendeeEntity.FIND_BY_INVITEE_ID,
					AttendeeEntity.class,
					new Param(AttendeeEntity.inviterProp, invitee));
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#findPrimaryKeys(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<Long> findPrimaryKeys(Integer inviterId, Integer groupId) {
		if (inviterId != null && groupId != null) {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ae.id FROM cal_entry_group ceg "); //
			query.append("JOIN ").append(AttendeeEntity.TABLE_NAME).append(" ae ");
			query.append("ON ae.inviter = ").append(inviterId).append(" ");
			query.append("AND ceg.cal_entry_group_id = ae.event_group_id ");
			query.append("JOIN cal_entry ce ");
			query.append("ON ce.ic_group_id = ").append(groupId).append(" ");
			query.append("AND ce.cal_entry_group_id = ceg.cal_entry_group_id ");

			List<Serializable[]> table = null;
			try {
				table = SimpleQuerier.executeQuery(query.toString(), 1);
			} catch (Exception e) {
				getLogger().log(Level.WARNING, 
						"Failed to execute query: '" + query + 
						"' cause of: ", e);
			}

			ArrayList<Long> primaryKeys = new ArrayList<Long>();
			if (!ListUtil.isEmpty(table)) {
				for (Serializable[] line : table) {
					for (Serializable column: line) {
						if (column instanceof Long) {
							primaryKeys.add((Long) column);
						}
					}
				}
			}

			return primaryKeys;
		}

		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#findBy(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<AttendeeEntity> findBy(Integer inviterId, Integer groupId) {
		List<Long> primaryKeys = findPrimaryKeys(inviterId, groupId);
		if (!ListUtil.isEmpty(primaryKeys)) {
			return findByPrimaryKeys(primaryKeys);
		}

		return Collections.emptyList();
	}

	@Override
	public AttendeeEntity findBy(User inviter, User invitee, Integer groupId) {
		return getSingleResult(
				AttendeeEntity.FIND_BY_ALL_PARAMETERS,
				AttendeeEntity.class,
				new Param(AttendeeEntity.inviterProp, inviter),
				new Param(AttendeeEntity.inviteeProp, invitee),
				new Param(AttendeeEntity.eventGroupIdProp, groupId));
	}

	/*
	 * (non-Javadoc)
	 * @see com.idega.block.calendar.data.dao.AttendeeDAO#remove(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void remove(Integer inviterId, Integer groupId) {
		if (inviterId != null && groupId != null) {
			List<AttendeeEntity> entities = findBy(inviterId, groupId);
			for (AttendeeEntity entity: entities) {
				remove(entity);
			}
		}
	}
}
