import React, { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import Card, { CardHeader, CardBody } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import { Calendar, MapPin, Bell, Tag, Sparkles, Clock, CheckCircle } from 'lucide-react';

const CATEGORIES = ['ALL', 'Hackathon', 'Symposium', 'Workshop', 'Sports', 'Cultural'];

export const StudentEventsPage = () => {
  const { data } = useOutletContext();
  const [selectedCategory, setSelectedCategory] = useState('ALL');

  const events = data?.events || [];
  const notices = data?.announcements || [];

  const filteredEvents = events.filter((e) => {
    if (selectedCategory === 'ALL') return true;
    return e.category?.toLowerCase() === selectedCategory.toLowerCase();
  });

  return (
    <div className="space-y-6">
      {/* Campus Events Section */}
      <Card>
        <CardHeader
          title="Campus Events, Hackathons & Activities"
          subtitle="Explore upcoming college hackathons, technical symposiums, and sports meets"
          action={
            <div className="flex items-center gap-1.5 p-1 rounded-xl bg-[var(--color-muted)]/40 border border-[var(--color-border)] overflow-x-auto no-scrollbar">
              {CATEGORIES.map((cat) => (
                <button
                  key={cat}
                  onClick={() => setSelectedCategory(cat)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all shrink-0 ${
                    selectedCategory === cat
                      ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                      : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                  }`}
                >
                  {cat}
                </button>
              ))}
            </div>
          }
        />
        <CardBody className="p-4">
          {filteredEvents.length === 0 ? (
            <p className="text-xs text-center text-[var(--color-muted-foreground)] py-8">
              No events found for category "{selectedCategory}".
            </p>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {filteredEvents.map((e) => (
                <div
                  key={e.id}
                  className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]/50 space-y-3 hover:border-[var(--color-primary)]/40 transition-colors"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-emerald-600 dark:text-emerald-400 px-2 py-0.5 rounded-full bg-emerald-500/10">
                      {e.category}
                    </span>
                    <span className="font-mono text-xs font-semibold text-[var(--color-muted-foreground)]">
                      {e.eventDate}
                    </span>
                  </div>

                  <div>
                    <h4 className="text-sm font-bold text-[var(--color-foreground)]">{e.title}</h4>
                    <p className="text-xs text-[var(--color-muted-foreground)] mt-1">{e.description}</p>
                  </div>

                  <div className="pt-2 border-t border-[var(--color-border)]/60 flex items-center justify-between text-xs">
                    <span className="text-[var(--color-muted-foreground)] flex items-center gap-1.5">
                      <MapPin className="w-3.5 h-3.5 text-[var(--color-primary)]" />
                      <strong>{e.location}</strong>
                    </span>
                    <span className="text-[10px] text-emerald-600 dark:text-emerald-400 font-semibold px-2 py-0.5 rounded bg-emerald-500/10">
                      Open for Registration
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardBody>
      </Card>

      {/* Campus Notices & Circulars */}
      <Card>
        <CardHeader
          title="Official Notices & Circulars"
          subtitle="Administrative and department circulars"
        />
        <CardBody className="p-0">
          <div className="divide-y divide-[var(--color-border)]">
            {notices.map((n) => (
              <div key={n.id} className="p-4 hover:bg-[var(--color-background)]/50 transition-colors space-y-1.5">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] uppercase font-bold text-[var(--color-primary)] px-1.5 py-0.5 rounded bg-[var(--color-primary)]/10">
                      {n.authorRole || 'ADMIN'}
                    </span>
                    <span className="text-[11px] text-[var(--color-muted-foreground)] font-mono">
                      {n.department || 'All Departments'}
                    </span>
                  </div>
                  <Badge variant={n.priority === 'URGENT' ? 'danger' : 'neutral'} size="sm">
                    {n.priority}
                  </Badge>
                </div>
                <h4 className="text-xs font-bold text-[var(--color-foreground)]">{n.title}</h4>
                <p className="text-xs text-[var(--color-muted-foreground)] leading-relaxed">{n.content}</p>
              </div>
            ))}
          </div>
        </CardBody>
      </Card>
    </div>
  );
};

export default StudentEventsPage;
