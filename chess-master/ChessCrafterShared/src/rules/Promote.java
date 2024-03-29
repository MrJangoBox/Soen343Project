package rules;

import java.util.List;

import javax.swing.JOptionPane;

import logic.Game;
import logic.Piece;
import logic.PieceBuilder;

public enum Promote
{
	CLASSIC, NO_PROMOTIONS;

	public Piece promotePiece(Piece pieceToPromote, boolean pieceCanBePromoted, String pieceTypeToPromoteFrom)
	{
		if (m_lastPromotedFromPieceName == null)
			m_lastPromotedFromPieceName = mResetLastPromoted;

		Piece promotedPiece;
		switch (this)
		{
		case CLASSIC:
			promotedPiece = classicPromotion(pieceToPromote, pieceTypeToPromoteFrom);
			break;
		case NO_PROMOTIONS:
			promotedPiece = pieceToPromote;
			break;
		default:
			promotedPiece = null;
		}

		mResetLastPromoted = m_lastPromotedFromPieceName;
		return promotedPiece;
	}

	public Piece undo(Piece pieceToUnpromote)
	{
		switch (this)
		{
		case CLASSIC:
			return classicUndo(pieceToUnpromote);
		case NO_PROMOTIONS:
			return pieceToUnpromote;
		default:
			return null;
		}
	}

	public void setGame(Game game)
	{
		mGame = game;
	}

	private Piece classicPromotion(Piece pieceToPromote, String pieceTypeToPromoteFrom)
	{
		if (pieceToPromote.getPromotesTo() == null || pieceToPromote.getPromotesTo().size() == 0)
		{
			m_lastPromotedFromPieceName = pieceToPromote.getName();
			mPromotedToClass = pieceToPromote.getName();
			return pieceToPromote;
		}
		else if (pieceTypeToPromoteFrom != null && !pieceTypeToPromoteFrom.equals(pieceToPromote.getName()))
		{
			// we don't want to promote the objective pieces. That makes things
			// weird...
			if ((pieceToPromote.isBlack() && !mGame.getBlackRules().getObjectiveName().equals(pieceToPromote.getName()))
					|| (!pieceToPromote.isBlack() && !mGame.getWhiteRules().getObjectiveName().equals(pieceToPromote.getName())))
			{
				try
				{
					Piece promoted = PieceBuilder.makePiece(pieceTypeToPromoteFrom, pieceToPromote.isBlack(),
							pieceToPromote.getSquare(), pieceToPromote.getBoard());
					if (promoted.isBlack())
						mGame.getBlackTeam().set(mGame.getBlackTeam().indexOf(pieceToPromote), promoted);
					else
						mGame.getWhiteTeam().set(mGame.getWhiteTeam().indexOf(pieceToPromote), promoted);
					promoted.getLegalDests().clear();
					promoted.setMoveCount(pieceToPromote.getMoveCount());
					return promoted;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		else if (pieceTypeToPromoteFrom == null && mGame.isBlackMove() == pieceToPromote.isBlack())
		{
			mPromotedToClass = ""; //$NON-NLS-1$
			if ((!pieceToPromote.isBlack() && pieceToPromote.getPromotesTo().size() == 1) || pieceToPromote.isBlack()
					&& pieceToPromote.getPromotesTo().size() == 1)
				mPromotedToClass = pieceToPromote.getPromotesTo().get(0);
			while (mPromotedToClass.equals("")) //$NON-NLS-1$
			{
				List<String> promotion = pieceToPromote.isBlack() ? pieceToPromote.getPromotesTo() : pieceToPromote.getPromotesTo();
				String result = (String) JOptionPane.showInputDialog(null,
						Messages.getString("selectPromotionType"), Messages.getString("promoChoice"), //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.PLAIN_MESSAGE, null, promotion.toArray(), null);

				if (result == null)
					continue;

				mPromotedToClass = result;
				pieceTypeToPromoteFrom = result;
			}
		}
		else if (pieceTypeToPromoteFrom != null && !pieceToPromote.isBlack() && pieceToPromote.getPromotesTo() != null
				&& pieceToPromote.getPromotesTo().contains(pieceTypeToPromoteFrom))
		{
			mPromotedToClass = pieceTypeToPromoteFrom;
		}
		else if (pieceTypeToPromoteFrom != null && pieceToPromote.isBlack() && pieceToPromote.getPromotesTo() != null
				&& pieceToPromote.getPromotesTo().contains(pieceTypeToPromoteFrom))
		{
			mPromotedToClass = pieceTypeToPromoteFrom;
		}

		try
		{
			Piece promoted = PieceBuilder.makePiece(mPromotedToClass, pieceToPromote.isBlack(), pieceToPromote.getSquare(),
					pieceToPromote.getBoard());
			if (promoted.isBlack())
				mGame.getBlackTeam().set(mGame.getBlackTeam().indexOf(pieceToPromote), promoted);
			else
				mGame.getWhiteTeam().set(mGame.getWhiteTeam().indexOf(pieceToPromote), promoted);
			promoted.getLegalDests().clear();
			promoted.setMoveCount(pieceToPromote.getMoveCount());
			return promoted;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return pieceToPromote;
		}
	}

	private Piece classicUndo(Piece pieceToUnpromote)
	{
		return classicPromotion(pieceToUnpromote, m_lastPromotedFromPieceName);
	}

	private static String m_lastPromotedFromPieceName;

	private Game mGame;
	private String mResetLastPromoted;
	private String mPromotedToClass;
}
